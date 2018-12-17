package controller.request;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;

import controller.request.interfaces.MessageListener;
import controller.request.interfaces.PublishSubscribe;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;

import static utils.GlobalConfig.PUBSUB_PORT;

public class Requester implements PublishSubscribe {

    final private Peer peer;
    final private PeerDHT _dht;

    final private ArrayList<String> s_topics = new ArrayList<String>();

    public Requester(final int _id, String _master_peer, final MessageListener _listener) throws IOException {
        peer = new PeerBuilder(Number160.createHash(_id)).ports(PUBSUB_PORT+_id).start();
        _dht = new PeerBuilderDHT(peer).start();

        FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(_master_peer)).ports(PUBSUB_PORT).start();
        fb.awaitUninterruptibly();
        if(fb.isSuccess()) {
            peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
        }

        peer.objectDataReply(new ObjectDataReply() {

            public Object reply(PeerAddress sender, Object request) throws Exception {
                return _listener.parseMessage(_id, request);
            }
        });
    }

    public boolean createTopic(String _topic_name){
        try {
            FutureGet futureGet = _dht.get(Number160.createHash(_topic_name)).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess() && futureGet.isEmpty())
                _dht.put(Number160.createHash(_topic_name)).data(new Data(new HashSet<PeerAddress>())).start().awaitUninterruptibly();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean subscribetoTopic(String _topic_name) {
        try {
            FutureGet futureGet = _dht.get(Number160.createHash(_topic_name)).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                if(futureGet.isEmpty() ) return false;
                HashSet<PeerAddress> peers_on_topic;
                peers_on_topic = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
                peers_on_topic.add(_dht.peer().peerAddress());
                _dht.put(Number160.createHash(_topic_name)).data(new Data(peers_on_topic)).start().awaitUninterruptibly();
                s_topics.add(_topic_name);
                return true;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean publishToTopic(String _topic_name, Object _obj) {
        try {
            FutureGet futureGet = _dht.get(Number160.createHash(_topic_name)).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                HashSet<PeerAddress> peers_on_topic;
                peers_on_topic = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
                for(PeerAddress peer:peers_on_topic)
                {
                    FutureDirect futureDirect = _dht.peer().sendDirect(peer).object(_obj).start();
                    futureDirect.awaitUninterruptibly();
                }

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}