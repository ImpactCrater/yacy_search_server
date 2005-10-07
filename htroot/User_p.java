//User_p.java 
//-----------------------
//part of the AnomicHTTPD caching proxy
//(C) by Michael Peter Christen; mc@anomic.de
//first published on http://www.anomic.de
//Frankfurt, Germany, 2004
//
//This File is contributed by Alexander Schier
//last major change: 30.09.2005
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//Using this software in any meaning (reading, learning, copying, compiling,
//running) means that you agree that the Author(s) is (are) not responsible
//for cost, loss of data or any harm that may be caused directly or indirectly
//by usage of this softare or this documentation. The usage of this software
//is on your own risk. The installation and usage (starting/running) of this
//software may allow other people or application to access your computer and
//any attached devices and is highly dependent on the configuration of the
//software which must be done by the user of the software; the author(s) is
//(are) also not responsible for proper configuration and usage of the
//software, even if provoked by documentation provided together with
//the software.
//
//Any changes to this file according to the GPL as documented in the file
//gpl.txt aside this file in the shipment you received can be done to the
//lines that follows this copyright notice here, but changes must not be
//done inside the copyright notive above. A re-distribution must contain
//the intact and unchanged copyright notice.
//Contributions and changes to the program code must be marked as such.


//You must compile this file with
//javac -classpath .:../Classes Message.java
//if the shell's current path is HTROOT

import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.Iterator;
import java.util.HashMap;
import java.io.IOException;

import de.anomic.plasma.plasmaSwitchboard;
import de.anomic.http.httpHeader;
import de.anomic.server.serverObjects;
import de.anomic.server.serverSwitch;
import de.anomic.data.userDB;
import de.anomic.server.serverCodings;

public class User_p {
    
    
    public static serverObjects respond(httpHeader header, serverObjects post, serverSwitch env) {
        serverObjects prop = new serverObjects();
        plasmaSwitchboard sb = plasmaSwitchboard.getSwitchboard();
        userDB.Entry entry=null;

        //default values
        prop.put("page", 0);
        prop.put("page_current_user", "newuser");
        prop.put("page_username", "");
        prop.put("page_firstname", "");
        prop.put("page_lastname", "");
        prop.put("page_address", "");
        prop.put("page_timelimit", "");
        prop.put("page_timeused", "");
        prop.put("page_timerange", "");
        prop.put("page_users", 0);

        if(sb.userDB == null)
            return prop;
        
        if(post == null){
			//do nothing
            
        //user != current_user
        //user=from userlist
        //current_user = edited user
		} else if(post.containsKey("user") && !((String)post.get("user")).equals("newuser")){
			if(post.containsKey("change_user")){
	            //defaults for newuser are set above
		        entry=sb.userDB.getEntry((String)post.get("user"));
			    //TODO: set username read-only in html
				prop.put("page_current_user", post.get("user"));
	            prop.put("page_username", post.get("user"));
		        prop.put("page_firstname", entry.getFirstName());
			    prop.put("page_lastname", entry.getLastName());
	            prop.put("page_address", entry.getAddress());
		        prop.put("page_timelimit", entry.getTimeLimit());
			    prop.put("page_timeused", entry.getTimeUsed());
			}else if( post.containsKey("delete_user") && !((String)post.get("user")).equals("newuser") ){
				sb.userDB.removeEntry((String)post.get("user"));
			}
        } else if(post.containsKey("change")) { //New User
            prop.put("page", 1); //results
            prop.put("page_text", 0);
            prop.put("page_error", 0);

            
            String username="";
            String pw="";
            String pw2="";
            String firstName="";
            String lastName="";
            String address="";
            String timeLimit="0";
            String timeUsed="0";
            HashMap mem=new HashMap();
            if( post.get("current_user").equals("newuser")){ //new user
                username=(String)post.get("username");
                pw=(String)post.get("password");
                pw2=(String)post.get("password2");
                if(! pw.equals(pw2)){
                    prop.put("page_error", 2); //PW does not match
                    return prop;
                }
                firstName=(String)post.get("firstname");
                lastName=(String)post.get("lastname");
                address=(String)post.get("address");
                timeLimit=(String)post.get("timelimit");
                timeUsed=(String)post.get("timeused");
                
				if(!pw.equals("")){ //change only if set
	                mem.put(userDB.Entry.MD5ENCODED_USERPWD_STRING, serverCodings.encodeMD5Hex(username+":"+pw));
				}
		        mem.put(userDB.Entry.USER_FIRSTNAME, firstName);
			    mem.put(userDB.Entry.USER_LASTNAME, lastName);
				mem.put(userDB.Entry.USER_ADDRESS, address);
				mem.put(userDB.Entry.TIME_LIMIT, timeLimit);
	            mem.put(userDB.Entry.TIME_USED, timeUsed);

                entry=sb.userDB.createEntry(username, mem);
                sb.userDB.addEntry(entry);
				prop.put("page_text_username", username);
				prop.put("page_text", 1);
                
            } else { //edit user
                username=(String)post.get("username");
                pw=(String)post.get("password");
                pw2=(String)post.get("password2");
                if(! pw.equals(pw2)){
                    prop.put("page_error", 1); //PW does not match
                    return prop;
                }
                firstName=(String)post.get("firstname");
                lastName=(String)post.get("lastname");
                address=(String)post.get("address");
                timeLimit=(String)post.get("timelimit");
                timeUsed=(String)post.get("timeused");

                entry = sb.userDB.getEntry(username);
				if(entry != null){
	                try{
		                entry.setProperty(userDB.Entry.MD5ENCODED_USERPWD_STRING, serverCodings.encodeMD5Hex(username+":"+pw));
			            entry.setProperty(userDB.Entry.USER_FIRSTNAME, firstName);
				        entry.setProperty(userDB.Entry.USER_LASTNAME, lastName);
					    entry.setProperty(userDB.Entry.USER_ADDRESS, address);
						entry.setProperty(userDB.Entry.TIME_LIMIT, timeLimit);
	                    entry.setProperty(userDB.Entry.TIME_USED, timeUsed);
		            }catch (IOException e){
					}
                }else{
					prop.put("page_error", 1);
				}
				prop.put("page_text_username", username);
				prop.put("page_text", 2);
            }//edit user
			prop.put("page_username", username);
        }
		
		//Generate Userlist
        Iterator it = sb.userDB.iterator(true);
        int numUsers=0;
        while(it.hasNext()){
            entry = (userDB.Entry)it.next();
            prop.put("page_users_"+numUsers+"_user", entry.getUserName());
            numUsers++;
        }
        prop.put("page_users", numUsers);

        // return rewrite properties
        return prop;
    }
}
