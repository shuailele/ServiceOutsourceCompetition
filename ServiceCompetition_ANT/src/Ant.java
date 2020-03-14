//规定客户数组中下标为0表示配送中心的位置
//BigDecimal类中 add加法 subtract减法 multiply乘法 divide除法
//当输入最大里程数小于全部距离时  程序会无响应  死循环

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Random;

public class Ant {                  //蚂蚁类

    private Random random;   //用于获取随机数
    private BigDecimal capacity;        //车的承载量
    private BigDecimal room;            //车辆空余的空间
    private static final double maxRemoving = Main.maxRemoving;     //车辆行驶的最大公里数
    private boolean allowChoseNextCity = true;      //是否允许访问下一个城市  true为允许  false为不允许
    private boolean destroy = false;                //当前路径是否恶意增加信息素
    private final int digit = 6;                          //表示小数保留位数
    public LinkedHashSet<Integer> path;               //辅助判断当前路径是否恶意增加信息素
    public static final double alpha = 2;            //地图上残留信息素对蚂蚁产生信息素的影响比例
    public static final double beta = 5;             //两点间距离对蚂蚁产生信息素的影响比例
    public static final double gamma = 3;            //载重率对蚂蚁选择下一城市的影响比例      (gamma / (alpha + beta + gamma) * (capacity - room) / capacity)

    public static final double pace = Main.pace;                    //车辆的行驶速度
    public double length;           //路径长度
    public ArrayDeque<Integer> tabu;     //配送顺序     包括了返程的路程
    public ArrayDeque<Double> remainingRoom;           //回到配送中心时车辆的满载率
    public int used = 0;                    //表示车辆被使用过的次数

    //默认构造函数
    public Ant(){
        this(5);            //指定车辆的默认容量为5
    }

    //可以指定承载量的构造函数
    public Ant(double capacity){
        if(capacity <= 0)
            throw new IllegalArgumentException("指定的车辆承载量错误");

        random = new Random(System.currentTimeMillis());        //使用系统时间作为种子
        this.capacity = new BigDecimal(Double.toString(capacity));
        room = new BigDecimal(Double.toString(capacity));
        path = new LinkedHashSet<>();
        length = 0;
        tabu = new ArrayDeque<>();
        remainingRoom = new ArrayDeque<>();
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
        this.capacity = new BigDecimal(Double.toString(capacity));
    }

    //获取承载量
    public double getCapacity(){
        return capacity.doubleValue();
    }

    //获取车内剩余空间
    public double getRoom(){
        return room.doubleValue();
    }

    //重置
    public void reset(){
        length = 0;

        tabu.clear();

        used = 0;

        remainingRoom.clear();

        path.clear();
    }

    //回到配送中心后的信息重置
    public void resetGo(){
        allowChoseNextCity = true;

        room = capacity;

        path.clear();
        tabu.clear();
        addPlace(0);
        length = 0;
    }

    //把地点place添加到已走过的数组中
    public double addPlace(int place){
        if(place < 0)
            throw new IllegalArgumentException("输入的地点存在错误");

        double pathLength = 0;

        if(!tabu.isEmpty()){
            pathLength = Map.distance[tabu.getLast()][place];   //将蚂蚁走的这段路径长度返回
            length += Map.distance[tabu.getLast()][place];      //更新路径长度
        }

        if(judgeDestroy(place)){
            //如果恶意破坏那么 将表示位置为true
            destroy = true;
        }

        tabu.add(place);

        if(room.compareTo(BigDecimal.valueOf(Map.cc[place].need)) >= 0 && Map.ok[place] == false){
            if(Map.cc[place].need == 0 && place != 0)
                return pathLength;
            Map.ok[place] = true;
            room = room.subtract(BigDecimal.valueOf(Map.cc[place].need));
        }

        return pathLength;
    }

    //蚂蚁在from到to之间的线路上撒下信息素
    public double antProduct(int from, int to){
        if(from < 0 || to < 0 || Map.distance[from][to] == 0)
            throw new IllegalArgumentException("客户索引输入存在错误");

        double p;       //下面这行根据两点间的距离越大且地图上的信息素浓度越大蚂蚁更渴望走这条路产生的信息素越多
        p = (alpha / (alpha + beta) * Map.pheromones[from][to]) + (beta / (alpha + beta) * (1 / Map.distance[from][to]));

        return p;
    }

    //选择下一城市2
    private int chooseNextPlace(){

        int to = -1;                        //要去的城市
        int curPlace = tabu.peekLast();     //当前的城市
        double hormone=0;	                //信息总量

        if(AntProject.count < 6){                   //当连续没产生更优解代数小于6时

            for(int i = 0 ; i < Map.clientCount + 1 ; ++i){         //计算和当前直接相连的点并且可以配送的点  （包括配送中心）
                if(Map.distance[curPlace][i] > 0 && Map.ok[i] == false &&
                        room.compareTo(BigDecimal.valueOf(Map.cc[i].need)) >= 0 && Map.distance[curPlace][i] <= maxRemoving){

                    hormone += (antProduct(curPlace, i) * (alpha + beta) +
                            (capacity.subtract(room.subtract(BigDecimal.valueOf(Map.cc[i].need)).divide(capacity, digit)).doubleValue() * gamma) / (alpha + beta + gamma));
                }
            }

            if(hormone == 0.0){                 //如果直接相连点全部无法配送

                while(true){
                    int place = rnd(Map.clientCount);

                    if(Map.distance[curPlace][place] > 0){
                        to = place;
                        break;
                    }
                }
            }
            else{                               //如果有直接相连的点可以配送    ！有也不一定走可以直达的点 但是走的概率大
//                int i = 0;
//                for(i = 0 ; i < Map.clientCount + 1 ; ++i){
//                    if(Map.distance[curPlace][i] > 0 && Map.distance[curPlace][i] <= maxRemoving){
//
//                        double curPlaceHormone = (antProduct(curPlace, i) * (alpha + beta)) / (alpha + beta + gamma);        //当前地点的信息素和满载率综合值
//                        if(Map.ok[i] == false)
//                            curPlaceHormone += ((capacity - (room - Map.cc[i].need)) / capacity * gamma) / (alpha + beta + gamma);//当前地点的信息素和满载率综合值
//
//                        if(Map.distance[curPlace][i] > 0 && (Map.ok[i] == true ||             //逻辑可能存在错误
//                                Map.cc[i].need > room) && Map.distance[curPlace][i] <= maxRemoving)
//                            curPlaceHormone *= 0.3;
//
//                        if(rnd(hormone) <= curPlaceHormone){
//                            return i;
//                        }
//                    }
//                }
//
//                if(i == Map.clientCount + 1){               //如果上一步选择失败则选择信息素和满载率综合值最大的走
//                    hormone = -1;
//                    for(int j = 0 ; j < Map.clientCount + 1 ; ++j)
//                        if(Map.distance[curPlace][j] > 0 && Map.ok[j] == false &&
//                                Map.cc[j].need <= room && Map.distance[curPlace][j] <= maxRemoving){
//
//                            double curPlaceHormone = (antProduct(curPlace, j) * (alpha + beta) +        //当前地点的信息素和满载率综合值
//                                    ((capacity - (room - Map.cc[j].need)) / capacity) * gamma) / (alpha + beta + gamma);
//
//                            if (hormone < curPlaceHormone){
//                                hormone = curPlaceHormone;
//                                to = j;
//                            }
//                        }
//                }

                to = roulette(curPlace);            //使用轮盘赌算法计算下一个城市
                if(to == -1)
                    throw new IllegalArgumentException("轮盘赌算法发生错误");
                return to;

            }
        }
        else{                                       //当连续没产生更优解代数大于等于6时
            for (int i = 0 ; i < Map.clientCount + 1 ; ++i) {
                if(Map.distance[curPlace][i] > 0 && Map.ok[i] == false &&
                        room.compareTo(BigDecimal.valueOf(Map.cc[i].need)) >= 0 && Map.distance[curPlace][i] <= maxRemoving) {
                    to = i;
                    AntProject.count = 0;
                    AntProject.rou = 0.7;

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
    public double mov(){
        return addPlace(chooseNextPlace());
    }

    //判断当前行走路径是否恶意破坏信息素平衡
    public boolean judgeDestroy(int nextPlace){           //true表示恶意破坏   false表示没有恶意破坏
        if (nextPlace < 0)
            throw new IllegalArgumentException("选择的下一个城市错误");

        if(Map.ok[nextPlace] == false && room.compareTo(BigDecimal.valueOf(Map.cc[nextPlace].need)) >= 0 && Map.cc[nextPlace].need != 0/* && nextPlace != 0*/){
            path.clear();
            path.add(nextPlace);
        }
        else{
            if(path.size() > 1 && path.peekFirst() != 0 && nextPlace == 0){
                path.clear();
                path.add(nextPlace);
                return false;
            }

            if(path.contains(nextPlace))
                return true;

            path.add(nextPlace);
        }

        return false;
    }

    //获取destroy的值  true代表当前路径为恶意增加信息素的路径 false反之
    public boolean getDestroy(){
        return destroy;
    }

    //设置destroy的值
    public void setDestroy(boolean value){
        destroy = value;
    }


    //轮盘赌算法
    public int roulette(int curPlace){
        double sum = 0;
        for(int i = 0 ; i < Map.clientCount + 1 ; ++i){
            if(Map.distance[curPlace][i] > 0 && Map.distance[curPlace][i] <= maxRemoving){

                double curPlaceHormone = (antProduct(curPlace, i) * (alpha + beta)) / (alpha + beta + gamma);        //当前地点的信息素和满载率综合值
                if(Map.ok[i] == false)
                    curPlaceHormone += (capacity.subtract(room.subtract(BigDecimal.valueOf(Map.cc[i].need))).divide(capacity, digit).doubleValue() * gamma)
                            / (alpha + beta + gamma);//当前地点的信息素和满载率综合值

                if(Map.distance[curPlace][i] > 0 && (Map.ok[i] == true ||             //逻辑可能存在错误
                        room.compareTo(BigDecimal.valueOf(Map.cc[i].need)) < 0) && Map.distance[curPlace][i] <= maxRemoving)
                    curPlaceHormone *= 0.3;

                sum += curPlaceHormone;
                }
            }

        double random = rnd(sum);

        double sum2 = 0;
        for(int i = 0 ; i < Map.clientCount + 1 ; ++i){
            if(Map.distance[curPlace][i] > 0 && Map.distance[curPlace][i] <= maxRemoving){

                double curPlaceHormone = (antProduct(curPlace, i) * (alpha + beta)) / (alpha + beta + gamma);        //当前地点的信息素和满载率综合值
                if(Map.ok[i] == false)
                    curPlaceHormone += (capacity.subtract(room.subtract(BigDecimal.valueOf(Map.cc[i].need))).divide(capacity, digit).doubleValue() * gamma)
                            / (alpha + beta + gamma);//当前地点的信息素和满载率综合值

                if(Map.distance[curPlace][i] > 0 && (Map.ok[i] == true ||             //逻辑可能存在错误
                        room.compareTo(BigDecimal.valueOf(Map.cc[i].need)) < 0) && Map.distance[curPlace][i] <= maxRemoving)
                    curPlaceHormone *= 0.3;

                sum2 += curPlaceHormone;

                if(random <= sum2)
                    return i;
            }
        }
        return -1;
    }

    //复制信息给当前蚂蚁
    public void copyInformation(Ant ant){
        room = capacity.subtract(ant.capacity.subtract(ant.room));

        length += ant.length;

        ArrayDeque<Integer> temp = ant.tabu.clone();
        if(!tabu.isEmpty())
            temp.removeFirst();

        while (!temp.isEmpty()){
            tabu.add(temp.removeFirst());
        }
        remainingRoom.add(capacity.subtract(room).divide(capacity, digit).doubleValue());
    }

    //获得随机数, 范围为 [low, uper]
    public double  rnd(double low, double uper)
    {
        if(low > uper || low < 0 || uper < 0)
            throw new IllegalArgumentException("输入的范围存在错误");

        return low + random.nextDouble() * uper % (uper - low + 1);
    }

    //获得随机数, 范围为 [0, uper)
    public double  rnd(double uper)
    {
        if(uper < 0)
            throw new IllegalArgumentException("输入的范围存在错误");

        return random.nextDouble() * uper;
    }

    //返回[0,uper]之间的整数
    public int rnd(int uper)
    {
        if(uper < 0)
            throw new IllegalArgumentException("输入范围存在错误");

        return random.nextInt(uper + 1);
    }

}
