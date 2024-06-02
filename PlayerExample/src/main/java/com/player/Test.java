package com.player;

import com.wavjaby.javaworld.adapter.PlayerApplication;
import com.wavjaby.javaworld.adapter.Self;
import com.wavjaby.javaworld.adapter.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test extends PlayerApplication {
    long time;

    @Override
    public void init(Self self) {
        World world = self.getWorld();
        time = world.getTime();
    }

    @Override
    public void gameUpdate(Self self) {
        World world = self.getWorld();
        if (world.getTime() - time > 1000) {
            time = world.getTime();
//            File file = new File("WantToGetFile");
//            System.out.println("0w0");
//            try {
//                self.getClass().getMethod("setPosition", Vector2D.class).invoke(self, new Vector2D(10, 10));
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