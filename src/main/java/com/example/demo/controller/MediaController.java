package com.example.demo.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class MediaController {


    @MessageMapping("/play")
    @SendTo("/media/play")
    public boolean Play(@Payload Map<String, Boolean> playedSent)
    {
        if(playedSent.get("playedSent"))
            return false;
        else
            return true;
    }
    @MessageMapping("/video")
    @SendTo("/media/video")
    public String SentVideo(@Payload Map<String, String> videoLink)
    {
        return videoLink.get("videoLink");
    }
    @MessageMapping("/setTime")
    @SendTo("/media/setTime")
    public long SetTime(@Payload Map<String, Long> videoTime)
    {
        return videoTime.get("videoTime");
    }
}
