package controller.request.interfaces;

public interface PublishSubscribe {

    public boolean createTopic(String _topic_name);
    public boolean subscribetoTopic(String _topic_name);
    public boolean publishToTopic(String _topic_name, Object _obj);

}