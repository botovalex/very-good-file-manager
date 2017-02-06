package com.github.botovalex;


import com.github.botovalex.controller.Controller;

public class Launcher {
    public static void main(String[] args) {
        Controller controller = new Controller();
        controller.init();
    }
}
