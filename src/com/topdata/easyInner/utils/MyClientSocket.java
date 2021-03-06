/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.topdata.easyInner.utils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.websocket.ClientEndpoint;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;

/**
 *
 * @author Claudemir Rtools
 */
@ClientEndpoint
public class MyClientSocket {

    static ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();

    private static final Set<Session> clients = Collections.synchronizedSet(new HashSet<Session>());

    //private static final Map<String, Date> sendRow = Collections.synchronizedMap(new LinkedHashMap<>());
    public MyClientSocket() {
        
    }

    // DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    @OnOpen
    public void open(Session session, @PathParam("identifier") String identifier) {
        clients.add(session);
    }

    @OnClose
    public void close(Session session) {
        clients.remove(session);
    }

    // @OnMessage
    public static void send(String identifier) {
        send(identifier, "1");
    }
    
    public static void send(String identifier, String text) {
        for (Session sess : clients) {
            if (sess.getRequestURI().getPath().contains(identifier)) {
                try {
                    sess.getBasicRemote().sendText(text);
                } catch (IOException ioe) {
                    System.out.println(ioe.getMessage());
                }
            }
        }
    }
}
