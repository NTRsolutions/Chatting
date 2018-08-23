package com.enhabyto.chatting;

public class RecyclerViewList {

    String phone_number, profile_image_url, profile_name, uid, name_saved_on_device, date_time, identity, message;

    public RecyclerViewList(){
        }



    public String getPhone_number(){ return phone_number; }

    public String getProfile_image_url(){ return profile_image_url; }

    public String getProfile_name(){ return profile_name; }

    public String getUid(){ return uid; }

    public String getName_saved_on_device(){ return name_saved_on_device; }

    public String getDate_time() {
        return date_time;
    }

    public String getIdentity() {
        return identity;
    }

    public String getMessage() {
        return message;
    }

    public void setPhone_number(String phone_number){ this.phone_number = phone_number; }

    public void setProfile_image_url(String profile_image_url){ this.profile_image_url = profile_image_url; }

    public void setProfile_name(String profile_name){ this.profile_name = profile_name; }

    public void setUid(String uid){ this.uid = uid; }

    public void setName_saved_on_device(String name_saved_on_device){ this.name_saved_on_device=name_saved_on_device; }


}
