
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ZParams;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Case01 {
    private static final int ITEMS_PER_PAGE = 25;
    private Jedis conn = null;

    public static final void main(String[] args) {
        new Case01().run();
    }

    public void run() {
        conn = new Jedis("localhost",6379);
        conn.auth("123456");

        test01();
        System.out.println("------------");
        test02();
        System.out.println("------------");
        test03();
        System.out.println("------------");
        test04();
        System.out.println("------------");
        test05();
        conn.close();

    }

    /**
     *测试字符串String
     */
    public void test01(){

        conn.set("name","zhouxy");
        conn.set("age","18");
        //失效时间1秒
        conn.expire("age",1);
        System.out.println(conn.get("name"));
        System.out.println(conn.exists("age"));
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(conn.get("age"));

    }

    /**
     *测试列表List
     */
    public void test02(){
        final String key = "SKILL";
        conn.lpush(key,"java","redis","Spring Boot");//从左边入队，多个item之间倒序排列
        conn.lpush(key,"MySQL");
        conn.rpush(key,"git");
        System.out.println(conn.lrange(key,0,-1));//[MySQL, Spring Boot, redis, java, git]
        System.out.println(conn.lpop(key));
        System.out.println(conn.rpop(key));
        System.out.println(conn.lrange(key,0,-1));//[Spring Boot, redis, java]
        conn.del(key);

    }

    /**
     *测试集合Set
     */
    public void test03(){
        final String key = "SKILL";
        conn.sadd(key,"java","java","Spring Boot");
        System.out.println(conn.smembers(key));
        System.out.println(conn.sismember(key,"java"));
        conn.del(key);
    }

    /**
     * 测试散列Hash
     */
    public void test04(){
        final String key = "SKILL";
        Map<String,String> map = new HashMap<String,String>();
        map.put("name","zhouxy");
        map.put("age","27");
        conn.hmset(key,map);
        conn.hincrBy(key,"age",1);//年龄增加1岁
        System.out.println(conn.hmget(key,"age"));
        conn.del(key);
    }

    /**
     * 测试有序集合zset
     */
    public void test05(){
        final String key = "LANGUAGE";//有序集合，存储所有语言及分值
        final String keyOOP = "OOP_LANGUAGE";//无序集合，存储面向对象语言
        final String orderOOP = "OOP_LANGUAGE_ORDER";//根据前两个集合得出交集
        conn.zadd(key,1.01,"java");
        conn.zadd(key,1.11,"c");
        conn.zadd(key,0.92,"c++");
        conn.zadd(key,0.83,"pathon");
        System.out.println(conn.zrange(key,0,-1));//[python, c++, java, c]
        System.out.println(conn.zscore(key,"java"));//java的分值
        System.out.println(conn.zrank(key,"java"));//java的排名

        conn.sadd(keyOOP,"java");
        conn.sadd(keyOOP,"c++");
        conn.sadd(keyOOP,"pathon");

        /*zinterstore取两个集合key和keyOOP的交集，将结果存储到orderOOP中
         *ZParams用来指明集合合并时,分值如何处理：SUM-求和、MIN-最小值、MAX-最大值
         * 对于无序的集合，默认分值都为1
         */
        ZParams params = new ZParams().aggregate(ZParams.Aggregate.SUM);
        conn.zinterstore(orderOOP,params,key,keyOOP);

        //并集结果输出（按分值升序）：[pathon, c++, java]
        System.out.println(conn.zrange(orderOOP,0,-1));

        //并集结果输出（按分值降序）：[{java=2.01}, {c++=1.92}, {pathon=1.83}]
        Set<String> languages = conn.zrevrange(orderOOP,0,-1);
        List<Map<String,Double>> list = new ArrayList<Map<String,Double>>();
        for (String item:languages) {
            Map<String,Double> tmpMap = new HashMap<String,Double>();
            tmpMap.put(item,conn.zscore(orderOOP,item));
            list.add(tmpMap);
        }
        System.out.println(list);

        conn.del(key);
        conn.del(keyOOP);
        conn.del(orderOOP);
    }
}
