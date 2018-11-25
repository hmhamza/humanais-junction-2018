import com.google.gson.annotations.SerializedName;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

interface VkAPI {
    String BASE_URL_RECEIVE_MSG="https://lp.vk.com/";
    String BASE_URL_SEND_MSG="https://api.vk.com/method/";

    @GET("wh174379225")
    Call<VkChat> getMessage(@Query("act") String act, @Query("ts") String timeStamp, @Query("wait") int wait, @Query("key") String key);

    @POST("messages.send")
    Call<Object> sendMessage(@Query("access_token") String token, @Query("peer_id") int peerId, @Query("random_id") int randomId, @Query("message") String message, @Query("group_id") int groupId, @Query("v") double version);
}

public class VkChat {

    private class Object {
        String text;
        String getText(){ return text; }
    }

    private class Updates {
        String type;
        Object object;

        String getText(){ return object.getText(); }

        String getType(){ return type; }
    }

    String ts;
    List<Updates> updates=null;

    public VkChat() {}

    String getTs(){
        return ts;
    }

    String getType(){
        if(updates!=null && updates.size()>0){
            return updates.get(0).getType();
        }return "NULL";
    }

    String getText(){
        if(updates!=null && updates.size()>0){
            return updates.get(0).getText();
        }return "NULL";
    }
}