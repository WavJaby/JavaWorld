package com.javaworld.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LiveCompilerTest {
    @Test
    public void addLoopInterrupt1() {
        String source = "for(Object a=new Object(){int a=0;public int hashCode(){while(a==1){}return a;}};a.hashCode()!=0;){}int a=1;{}for(;a!=1;){}";
        String answer = "for(Object a=new Object(){int a=0;public int hashCode(){while(isNotInterrupt()&&(a==1)){}return a;}};isNotInterrupt()&&(a.hashCode()!=0);){}int a=1;{}for(;isNotInterrupt()&&(a!=1);){}";

        System.out.println(source);
        String result = LiveCompiler.addLoopInterrupt(source);
        System.out.println(result);
        assertEquals(result, answer);
    }

    @Test
    public void addLoopInterrupt2() {
        String source = "for(int a=1;new Object(){public String toString(){while(true){return\"\";}}}.toString().equals(\"T\");a++){}for(int a=1;a!=1;a++){}";
        String answer = "for(int a=1;isNotInterrupt()&&(new Object(){public String toString(){while(isNotInterrupt()&&(true)){return\"\";}}}.toString().equals(\"T\"));a++){}for(int a=1;isNotInterrupt()&&(a!=1);a++){}";
        System.out.println(source);
        String result = LiveCompiler.addLoopInterrupt(source);
        System.out.println(result);
        assertEquals(result, answer);
    }

    @Test
    public void addLoopInterrupt3() {
        String source = "for(int a=1; a!=1 ;new Object(){int a=0;}.a++)a+=1;for(;;)a+=1;";
        String answer = "for(int a=1;isNotInterrupt()&&( a!=1 );new Object(){int a=0;}.a++)a+=1;for(;isNotInterrupt();)a+=1;";

        System.out.println(source);
        String result = LiveCompiler.addLoopInterrupt(source);
        System.out.println(result);
        assertEquals(result, answer);
    }

    @Test
    public void addLoopInterrupt4() {
        String source = "while(new Object(){public String toString(){int a=1;while(a==1){return\"\";}return\"T\";}}.toString().equals(\"T\")){}int a=1;do{}while(a!=1);";
        String answer = "while(isNotInterrupt()&&(new Object(){public String toString(){int a=1;while(isNotInterrupt()&&(a==1)){return\"\";}return\"T\";}}.toString().equals(\"T\"))){}int a=1;do{}while(isNotInterrupt()&&(a!=1));";

        System.out.println(source);
        String result = LiveCompiler.addLoopInterrupt(source);
        System.out.println(result);
        assertEquals(result, answer);
    }

    @Test
    public void addLoopInterrupt5() {
        String source = "for (int i : integers) {System.out.println(i);}";
        String answer = "for (int i : integers) {System.out.println(i);}";

        System.out.println(source);
        String result = LiveCompiler.addLoopInterrupt(source);
        System.out.println(result);
        assertEquals(result, answer);
    }
}
