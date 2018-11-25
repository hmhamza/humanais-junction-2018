import com.google.gson.Gson;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.queries.groups.GroupsGetLongPollServerQuery;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Random;

class Main {

    private static String ACCESS_TOKEN="<ACCESS TOKEN>";

    private static class Message{
        String text;
        String author;

        public Message(String text, String author) {
            this.text = text;
            this.author = author;
        }

        public String getText() {
            return text;
        }

        public String getAuthor() {
            return author;
        }
    }
    public static int getRandomId() {
        Random rand=new Random();

        int min=10000,max=1000000;
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    public static Message getReplyFromAgent(String question) {

        String answer="Sorry. I can't answer right now.";

        if(question.matches("is it better than proposal from bank xyz\\?")
                || question.matches("give more details")){
            answer="Well, they have lower percentage, however, more strict rules and approach of  control. If you are interested to discover more info about it, please click this link.";
        }
        Message msg=new Message(answer,"Agent");
        return msg;
    }

    public static Message getReply(String question) {

        Message msg=null;

        String answer="NULL";
        question=question.toLowerCase();
        if(question.matches("hi") || question.matches("hello") || question.matches("hi there") || question.matches("hi there!")){
            answer="Hi! How could I help you?";
        }
        else if(question.matches("tell me please about your product for it entrepreneurs")
                || question.matches("tell me about your product")){
            answer="Sure! We have very cheap loan for IT projects! You could read detail here.";
        }
        else if(question.matches("okay, thank you. and what about maximum period of loan\\?")){
            answer="2 years";
        }
        else if(question.matches("thanks") || question.matches("thanks!")){
            answer="You are Welcome!";
        }
        else{
            msg=getReplyFromAgent(question);
            return msg;
        }

        msg=new Message(answer,"Bot");
        return msg;
    }

    public static void main(String[] args) {
        TransportClient transportClient = HttpTransportClient.getInstance();
        VkApiClient vk = new VkApiClient(transportClient, new Gson(),1);

        GroupActor ga=new GroupActor(<GROUP_ID>,ACCESS_TOKEN);
        GroupsGetLongPollServerQuery query=vk.groups().getLongPollServer(ga);

        String key=null,server=null,ts=null;
        try {
            String response=query.execute().toString();
            key=response.substring(response.indexOf("key")+5,response.indexOf("server")-3);
            //server=response.substring(response.indexOf("server")+8,response.indexOf("ts=")-3);
            ts=response.substring(response.indexOf("ts=")+3,response.length()-1);

            //System.out.println("Result: "+response);
            //System.out.println("Key: *"+key+"*");
            //System.out.println("Server: *"+server+"*");
            //System.out.println("TS: *"+ts+"*");
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }

        Retrofit retrofit_receiveMsg = new Retrofit.Builder()
                .baseUrl(VkAPI.BASE_URL_RECEIVE_MSG)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        VkAPI api_receiveMsg=retrofit_receiveMsg.create(VkAPI.class);

        Retrofit retrofit_sendMsg = new Retrofit.Builder()
                .baseUrl(VkAPI.BASE_URL_SEND_MSG)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        VkAPI api_sendMessage=retrofit_sendMsg.create(VkAPI.class);

        System.out.println("SERVER STARTED!");
        try {
            String prevTs=ts,newTs;
            while(true){
                Response<VkChat> response = api_receiveMsg.getMessage("a_check",prevTs,1,key).execute();
                VkChat chat=response.body();
                newTs=chat.getTs();

                if(chat.getType().matches("message_new") && !newTs.matches(prevTs)){
                    //System.out.println("* New Message * TS: "+newTs+" Text: "+chat.getText());

                    Message answer=getReply(chat.getText());

                    api_sendMessage.sendMessage(
                            ACCESS_TOKEN,<PEER_ID>,getRandomId(),answer.getText(),<GROUP_ID>,5.92).execute();

                    System.out.println("\n* New Message *");
                    System.out.println("TS: "+newTs);
                    System.out.println("Received: "+chat.getText());
                    System.out.println("Sent: "+answer.getText());
                    System.out.println("By: "+answer.getAuthor());
                }
                prevTs=newTs;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("ENDED!");
    }
}
