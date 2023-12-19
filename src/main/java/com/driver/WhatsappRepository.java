package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below-mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;//contains every user of a group
    private HashMap<Group, List<Message>> groupMessageMap;//contains every message of group
    private HashMap<Message, User> senderMap;// Message -> sender map
    private HashMap<Group, User> adminMap;//Group -> admin mapping
    private HashSet<String> userMobile;// all mobile strings(no.)
    private HashSet<Message> messages;
    private int customGroupCount;// Group number for users more than 2
    private int messageId;//auto increment on every new msg

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.messages = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    WhatsappRepository whatsappRepository = new WhatsappRepository();

    public String createUser(String name, String mobile) throws Exception {
        //If the mobile number exists in database, throw "User already exists" exception
        //Otherwise, create the user and return "SUCCESS"

        for(String mobNo : userMobile){
            if(mobNo.equals(mobile)){
                throw new Exception("User already exist");
            }
        }
        //create user
        User user = new User(name,mobile);
        userMobile.add(mobile);

        return "SUCCESS";

    }

    public Group createGroup(List<User> users){
        // The list contains at least 2 users where the first user is the admin. A group has exactly one admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group count". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
        // Note that a personal chat is not considered a group and the count is not updated for personal chats.
        // If group is successfully created, return group.

        //For example: Consider userList1 = {Alex, Bob, Charlie}, userList2 = {Dan, Evan}, userList3 = {Felix, Graham, Hugh}.
        //If createGroup is called for these userLists in the same order, their group names would be "Group 1", "Evan", and "Group 2" respectively.
        int size = users.size();
        User groupAdmin = users.get(0);
        String groupName = "";
        if(users.size()==2){
           groupName = users.get(1).getName();
        }else{
            this.customGroupCount++;
            groupName =  "Group "+customGroupCount;
        }
        Group newGroup = new Group(groupName,size);
        //map group to user list
        groupUserMap.put(newGroup,users);
        adminMap.put(newGroup,groupAdmin);
        return newGroup;
    }

    public int createMessage(String content){
        // The 'i^th' created message has message id 'i'.
        // Return the message id.
        this.messageId++;
        Message newMsg = new Message(messageId,content);
        messages.add(newMsg);
        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.
        if(!groupUserMap.containsKey(group))throw new Exception("Group does not exist");
        List<User> userList = groupUserMap.get(group);
        boolean userExist = false;
        for(User user :userList){
            if(user.getName().equals(sender.getName()))
            {
                userExist=true;
                break;
            }
        }
        if(!userExist)throw new Exception("You are not allowed to send message");
        List<Message> msgList = groupMessageMap.get(group);
        msgList.add(message);
        senderMap.put(message,sender);
        return msgList.size();
    }
    public String changeAdmin(User approver, User user, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS". Note that at one time there is only one admin and the admin rights are transferred from approver to user.
        if(!groupUserMap.containsKey(group))throw new Exception("Group does not exist");

        String admin = groupUserMap.get(group).get(0).getName();
        if(!admin.equals(approver)) throw new Exception("Approver does not have rights");

        List<User> userList = groupUserMap.get(group);
        boolean userExist = false;
        for(User usr : userList){
            if(user.getName().equals(usr.getName())){
                userExist=true;
                break;
            }
        }
        if(!userExist)throw new Exception("User is not a participant");
        adminMap.put(group,approver);
        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception{
        //This is a bonus problem and does not contains any marks
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)
        boolean userFound = false;
        Group userGroup = null;
        for (Group grp : groupUserMap.keySet()){
            List<User> userList = groupUserMap.get(grp);
            for(User usr : userList){
                if(usr.getName().equals(user.getName())){
                    userGroup=grp;
                    userFound=true;
                    break;
                }
            }
        }
        if(!userFound)throw new Exception("User not found");
        if(adminMap.get(userGroup).getName().equals(user.getName())) throw new Exception("Cannot remove admin");

        // delete user , all user messages from user group and remove user from all Mapping

        //remove user
        String userMob = user.getMobile();
        userMobile.remove(userMob);

        // remove user from sender map
        HashSet<Message> userMsg = new HashSet<>();
        for(Message msg : senderMap.keySet()){
            User usr = senderMap.get(msg);
            if(usr.getName().equals(user.getName())){
                userMsg.add(msg);
                senderMap.remove(msg);
            }
        }
        //remove user message from its group
        List<Message> grpMsgList = groupMessageMap.get(userGroup);
        for(Message msg : grpMsgList){
            if(userMsg.contains(msg)){
                grpMsgList.remove(msg);
            }
        }

        //remove user from groupUserMap
        List<User> userList = groupUserMap.get(userGroup);
        for(User usr : userList){
            if(usr.getName().equals(user.getName())){
                userList.remove(usr);
                break;
            }
        }
        return userList.size() + grpMsgList.size() + messages.size();
    }

    public String findMessage(Date start, Date end, int K) throws Exception{
        //This is a bonus problem and does not contains any marks
        // Find the Kth latest message between start and end (excluding start and end)
        // If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception
        String kthMsg="";
        List<String> li = new ArrayList<>();
        for(Message msg : messages){
            if(msg.getTimestamp().after(start) && msg.getTimestamp().before(end)){
                li.add(msg.getContent());
            }
        }
        if(K>li.size())throw new Exception("K is greater than the number of messages");

        return li.get(K-1);
    }
}
