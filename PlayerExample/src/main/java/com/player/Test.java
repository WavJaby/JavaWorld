package com.player;

import com.javaworld.adapter.PlayerApplication;
import com.javaworld.adapter.Self;
import com.javaworld.adapter.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test extends PlayerApplication {
    long time;

    @Override
    public void init(Self self) {
        World world = self.getWorld();
        time = world.getWorldTime();
    }

    @Override
    public void gameUpdate(Self self) {
        World world = self.getWorld();
        if (world.getWorldTime() - time > 1000) {
            time = world.getWorldTime();
//            File file = new File("WantToGetFile");
//            System.out.println("0w0");
//            try {
//                self.getClass().getMethod("setPosition", Vec2.class).invoke(self, new Vec2(10, 10));
//            } catch (Exception e) {
//            }
//            boolean a = true;
//            while (a) {
//            }

            Map<String, A> map = new HashMap<>();
            List<Map<String, A>> list = new ArrayList<>();
            list.add(map);
            map.put("abc", new A("0w0"));
            console.println(list.get(0).get("abc").a);
        }
    }

    public static class A {
        String a;

        public A(String a) {
            this.a = a;
        }
    }
}