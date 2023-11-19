package indiv.hmdp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringTest {
    public static void main(String[] args) {
//        String s="1885460";
//        if (s.indexOf("*")>0){
//            System.out.println(s);
//        }
        List<String> strings=new ArrayList<>();
//        strings.add("aaa");
//        strings.add("cc");
//        strings.add("kkk");
        String s = Arrays.toString(strings.toArray());
        System.out.println(s);
    }

}
