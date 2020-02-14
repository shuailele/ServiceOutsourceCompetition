//规定客户数组中下标为0表示配送中心的位置

//当输入最大里程数小于全部距离时  程序会无响应  死循环


import java.util.ArrayDeque;
import java.util.Random;

public class Ant {                  //蚂蚁类

    private static Random random;   //用于获取随机数
    private double capacity;        //车的承载量
    private double room;            //车辆空余的空间
    private boolean pass[];         //车辆回配送中心允许经过的点     false表示未经过 true表示经过
    private static final double maxRemoving = Main.maxRemoving;     //车辆行驶的最大公里数
    private boolean allowChoseNextCity = true;      //是否允许访问下一个城市  true为允许  false为不允许

    public static final double pace = Main.pace;                    //车辆的行驶速度
    public double length;           //路径长度
    public ArrayDeque<Integer> tabu;     //配送顺序     包括了返程的路程

    //默认构造函数
    public Ant(){
        this(5);            //指定车辆的默认容量为5
    }

    //可以指定承载量的构造函数
    public Ant(double capacity){
        if(capacity <= 0)
            throw new IllegalArgumentException("指定的车辆承载量错误");

        random = new Random(System.currentTimeMillis());        //使用系统时间作为种子
        this.capacity = capacity;
        room = capacity;
        pass = new boolean[Map.clientCount + 1];
        length = 0;
        tabu = new ArrayDeque<>();

        for(int i = 0 ; i < Map.clientCount + 1 ; ++i)
            pass[i] = false;
    }

    //设置是否允许访问下一个城市
    public void setAllowChoseNextCity(boolean allow){
        allowChoseNextCity = allow;
    }

    //获取是否允许访问下一个城市
    public boolean getAllowChoseNextCity(){
        return allowChoseNextCity;
    }

    //设置承载量
    public void setCapacity(double capacity){
        this.capacity = capacity;
    }

    //获取承载量
    public double getCapacity(){
        return capacity;
    }

    //获取车内剩余空间
    public double getRoom(){
        return room;
    }

    //重置
    public void reset(){
        length = 0;

        for(int i = 0 ; i < Map.clientCount + 1 ; ++i)
            pass[i] = false;

        room = capacity;

        allowChoseNextCity = true;

        tabu.clear();

        addPlace(0);
    }

    //重置每只蚂蚁的车辆空间
    public void resetRoom(){
        room = capacity;
    }

    //把地点place添加到已走过的数组中
    public void addPlace(int place){
        if(place < 0)
            throw new IllegalArgumentException("输入的地点存在错误");

        if(!tabu.isEmpty())
            length += Map.distance[tabu.getLast()][place];      //更新路径长度
        tabu.add(place);

        if(place != 0 && room >= Map.cc[place].need){
            Map.ok[place] = true;
            room -= Map.cc[place].need;
        }

    }

    //蚂蚁在from到to之间的线路上撒下信息素
    public double antProduct(int from, int to){
        if(from < 0 || to < 0)
            throw new IllegalArgumentException("客户索引输入存在错误");

        double p;       //下面这行根据两点间的距离越大且地图上的信息素浓度越大蚂蚁更渴望走这条路产生的信息素越多
        p = Math.pow((1.0 / Map.distance[from][to]), 5) * Math.pow((Map.pheromones[from][to]), 1);	//转移期望
        if( p <= 0)
            p = rnd(0,1) * Math.pow( (1.0 / Map.distance[from][to]), 5);  //如果没有其他蚂蚁走过，就按距离选择

        return p;
        //return 1.0 / Map.distance[from][to];
    }

    //选择下一个地点
    public int chooseNextPlace(){
        int to = -1;                                //to为下一个要到的城市
        int curPlace = tabu.getLast();              //当前城市
        int i;
        double hormone=0;	//信息总量

        if(AntProject.count < 6){
            for(i = 1 ; i < Map.clientCount + 1 ; ++i)
                if(Map.cc[i].need != 0 && Map.distance[curPlace][i] > 0 && Map.ok[i] == false &&
                        Map.cc[i].need <= room && Map.distance[curPlace][i] <= maxRemoving)
                    hormone += antProduct(curPlace, i);         //计算信息总量

//            if(hormone == 0.0 && Map.distance[curPlace][0] > 0 && (Map.cc[0].need >= room || Map.distance[curPlace][0] > maxRemoving))
//                hormone += antProduct(curPlace, 0);

            if(hormone == 0.0){           //如果所有顾客配送完成
                while(true){
                    if(Map.distance[curPlace][0] > 0){
                        to = 0;
                        break;
                    }

                    int place = rnd(Map.clientCount);
                    if(Map.distance[curPlace][place] > 0 && pass[place] == false){
                        pass[curPlace] = true;
                        pass[place] = true;
                        to = place;
                        break;
                    }
                }
            }
            else{
                for(to = 1 ; to < Map.clientCount + 1 ; ++to){
                    if(Map.distance[curPlace][to] > 0 && Map.ok[to] == false &&
                            Map.cc[to].need <= room && Map.distance[curPlace][to] <= maxRemoving){
                        double p = antProduct(curPlace,to) / hormone;
                        if(rnd(0,1) < p)	    //按转移概率大小选择下一个城市，同时也避免了局部最优解
                            break;
                    }
                }
            }

            if(to == Map.clientCount + 1){
                hormone = -1;
                for(i = 1 ; i < Map.clientCount + 1 ; ++i)
                    if(Map.distance[curPlace][i] > 0 && Map.ok[i] == false &&
                            Map.cc[i].need <= room && Map.distance[curPlace][i] <= maxRemoving)
                    {
                        if (hormone < antProduct(curPlace, i)) {
                            hormone = antProduct(curPlace,i);	//如果上一步选择失败，则选择具有最大信息量的城市
                            to = i;
                        }
                    }
            }
        }
        else{                   //当连续6代都没有产生更短的路径，则
            for ( i = 1 ; i < Map.clientCount + 1 ; ++i) {
                if(Map.distance[curPlace][i] > 0 && Map.ok[i] == false &&
                        Map.cc[i].need <= room && Map.distance[curPlace][i] <= maxRemoving) {
                    to = i;
                    AntProject.count = 0;
                    AntProject.rou = 0.5;

                    if(rnd(0.0,1.0) > 0.5)	//随机选择一个还没访问的城市
                    {
                        break;
                    }
                }
            }
        }
        return to;
    }

    //蚂蚁向下一个位置移动一步
    public void mov(){
        addPlace(chooseNextPlace());
    }



    //获得随机数, 范围为 [low, uper]
    public static double  rnd(double low, double uper)
    {
        if(low > uper || low < 0 || uper < 0)
            throw new IllegalArgumentException("输入的范围存在错误");

        return (random.nextDouble() * (uper - low + 1) + low) % (uper - low + 1);
    }

    //返回[0,uper]之间的整数
    public static int rnd(int uper)
    {
        if(uper < 0)
            throw new IllegalArgumentException("输入范围存在错误");

        return random.nextInt(uper + 1);
    }

}
