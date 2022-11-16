package blog.cosmos.home.generativeai;

public class Message {

    // string to store our message and sender
    private String message;
    private String sender;

    private String imageUrl;

    // constructor.

    public Message(String message, String sender, String imageUrl) {
        this.message = message;
        this.sender = sender;
        this.imageUrl = imageUrl;
    }


    // getter and setter methods.


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
