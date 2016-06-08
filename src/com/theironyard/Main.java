package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    static HashMap<String, User> users = new HashMap<>();
    static ArrayList<Message> messages = new ArrayList<>();

    public static void main(String[] args) {
	    addTestUsers();
        addTestMessages();

        Spark.init(); // not necessary, but zach usually puts in through habit

        Spark.get(
                "/",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");

                    String idStr = request.queryParams("replyId");
                    int replyId = -1;
                    if (idStr != null) {
                        replyId = Integer.valueOf(idStr);
                    }


                    ArrayList<Message> subset = new ArrayList<Message>();
                    for (Message msg : messages) {
                        if (msg.replyId == replyId) {
                            subset.add(msg);
                        }
                    }

                    HashMap m = new HashMap();
                    m.put("messages", subset);
                    m.put("username", username);
                    m.put("replyId", replyId);
                    return new ModelAndView(m, "home.html");
                },
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/login",
                (request, response) -> {
                    String username = request.queryParams("username");
                    if (username == null) {
                        throw new Exception("Login name not found");
                    }

                    User user = users.get(username);
                    if (user == null) {
                        user = new User(username, "");
                        users.put(username, user);
                    }

                    Session session = request.session();
                    session.attribute("username", username);

                    response.redirect(request.headers("Referer"));
                    return "";
                }
        );
        Spark.post(
                "/logout",
                (request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "/create-message",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    if (username == null) {
                        throw new Exception("Not logged in");
                    }

                    int replyId = Integer.valueOf(request.queryParams("replyId"));
                    String text = request.queryParams("message");

                    Message msg = new Message(messages.size(), replyId, username, text);
                    messages.add(msg);

                    response.redirect(request.headers("Referer"));
                    return "";
                }
        );
    }

    static void addTestUsers() {
        users.put("Alice", new User("Alice", ""));
        users.put("Bob", new User("Bob", ""));
        users.put("Charlie", new User("Charlie", ""));
    }

    static void addTestMessages() {
        messages.add(new Message(0, -1, "Alice", "Hello, world!"));
        messages.add(new Message(1, -1, "Bob", "This a new thread!"));
        messages.add(new Message(2, 0, "Charlie", "Cool thread, Alice!"));
    }
}
