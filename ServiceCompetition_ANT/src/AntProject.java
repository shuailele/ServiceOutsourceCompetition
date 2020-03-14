

import java.util.ArrayDeque;
import java.util.ArrayList;

public class AntProject {

    Map map;                                  //地图类
    public static int count = 0;              //连续多少代没有产生更优解
    private final int []antCount = Main.carCount;           //蚂蚁的数量
    private final double []antCapacity = Main.carCapacity;          //蚂蚁的承载量
    private final int antType = Main.carType;                   //蚂蚁的种类
    private int realAntCount = antCount[0];                    //实际使用的车辆数量
    public static double rou = 0.5;            //信息素的挥发系数
    private final int itCount = 500;           //最大迭代次数

    public int getItCount(){        //用于曲线图
        return itCount;
    }           //构造曲线图用

    private double bestMethod;                 //记录最佳方案的综合值
    private double bestLength;                  //记录最优方案的路径长度
    private ArrayList<String> bestTabu;           //记录最佳方案
    private double carryTime;                     //运送时间
    private final int multiple = 3;                 //长度倍数
    private Ant ant[];

    public double itorBestLength[];             //记录每次迭代后的最佳路径长度        构造曲线图用

    //构造函数
    public AntProject(){
        map = new Map();
        initMap();                              //初始化地图
        bestMethod = Double.MAX_VALUE;
        bestLength = Double.MAX_VALUE;
        bestTabu = new ArrayList<>();
        ant = new Ant[antCount[0] + 1];         //蚂蚁0表示最大承载量的蚂蚁     用于模拟行走路线
        int curCount = 0;
        for(int i = 1 ; i <= antType ; ++i){
            for(int j = 1 + curCount ; j <= antCount[i] + curCount ; ++j){
                ant[j] = new Ant(antCapacity[i]);
            }
            curCount += antCount[i];
        }
        ant[0] = new Ant(antCapacity[0]);

        initAnt();                              //初始化蚁群

        itorBestLength = new double[itCount];       //构造折线图使用
    }

    //重置地图信息
    public void reset(){
        for(int i = 0 ; i < Map.clientCount + 1 ; ++i)
            Map.ok[i] = false;
    }

    //获取蚁群的数量
    public int getAntCount(){
        return antCount[0];
    }

    //获取运送时间
    public double getCarryTime(){
        return carryTime;
    }

    //设置每辆车的承载量
    public void setAllAntCapacity(){
        for(int i = 0 ; i < antCount[0] ; ++i)
            ant[i].setCapacity(5);
    }

    //初始化地图
    private void initMap(){
        for(int i = 0 ; i < Map.clientCount + 1 ; ++i)
            for(int j = 0 ; j < Map.clientCount + 1 ; ++j){
                Map.pheromones[i][j] = 1;
                Map.pheromonesChange[i][j] = 0;
            }
    }

    //初始化蚁群
    public void initAnt(){
        ant[0].addPlace(0);
    }

    //迭代搜索
    public void startSearch(){
        int max = 0;                                //当前的代数
        int tempRealCount;

        while(max < itCount){
            tempRealCount = 0;
            double tempLength = 0;
            double tempRemainingRoom = 0;       //每次路线的满载率之和

            int []tempAntCount = antCount.clone();      //车辆数量的数组  用于底下的更改

            Beyond:
            while(!Map.end()){
                //一下为更改的车辆选择
                    while(ant[0].getAllowChoseNextCity()){
                        tempLength += ant[0].mov();

                        if(ant[0].getDestroy()){                //如果该车辆行驶属于恶意破坏信息素平衡，则该车辆回退
                            int last = ant[0].tabu.peekLast();

                            int toed = ant[0].tabu.removeLast();
                            int fromed = ant[0].tabu.peekLast();
                            tempLength -= Map.distance[fromed][toed];
                            ant[0].length -=Map.distance[fromed][toed];

                            while (ant[0].path.size() > 1 && ant[0].path.peekLast() != last){

                                int to = ant[0].path.peekLast();

                                ant[0].path.removeLast();
                                if(ant[0].path.isEmpty())
                                    break;
                                int from = ant[0].path.peekLast();

                                tempLength -= Map.distance[from][to];
                                ant[0].length -= Map.distance[from][to];
                                if(ant[0].tabu.size() > 1)
                                    ant[0].tabu.removeLast();
                            }

                            ant[0].setDestroy(false);
                            continue ;
                        }

                        if(tempLength > (multiple * bestLength)){
                            break ;
                            //如果此次方案长度太长则舍弃此次方案
                        }

                        if(ant[0].tabu.peekLast() == 0 && ant[0].tabu.size() != 1)
                            ant[0].setAllowChoseNextCity(false);
                    }

                    int chooseCapacity = 0;
                    for(int i = 1 ; i <= antType ; ++i){            //将正确的车型选择出来
                        if(ant[0].getCapacity() - ant[0].getRoom() <= antCapacity[i] && antCapacity[i] <= antCapacity[chooseCapacity])
                            chooseCapacity = i;
                    }

                     int startSite = 1;
                      for(int i = 1 ; i <= antType ; ++i){
                           if(antCapacity[i] == antCapacity[chooseCapacity])
                                break ;
                          startSite += antCount[i];
                      }

                     int chooseAnt = startSite;

                    if(tempAntCount[chooseCapacity] <= 0){          //如果选择的车型数量不足   证明该车型车辆所有都被使用过
                        int minUsed = ant[chooseAnt].used;
                        for(int i = startSite ; i < antCount[chooseCapacity] + startSite ; ++i){
                            if(ant[i].used < minUsed){
                                chooseAnt = i;
                                break ;
                            }
                        }

                        ++ant[chooseAnt].used;
                    }
                    else{
                        for(int i = startSite ; i < antCount[chooseCapacity] + startSite ; ++i){
                            if(ant[i].used == 0){      //选择出该使用的车辆并且该车没被使用过
                                chooseAnt = i;
                                ++ant[i].used;
                                --tempAntCount[chooseCapacity];
                                break ;
                            }
                        }
                    }


                    ant[chooseAnt].copyInformation(ant[0]);

                    ant[0].resetGo();

                    if(tempLength > (multiple * bestLength)){
                        //如果此次方案长度太长则舍弃此次方案
                        break Beyond;
                    }

                    if(Map.end()){
                        for(int i = 1 ; i <= antType ; ++i){
                            tempRealCount += antCount[i] - tempAntCount[i];
                        }

                        break;
                    }


                //以上为更改过得车辆选择
            }

            if(tempLength > (multiple * bestLength)){                    //如果此次方案长度太长则舍弃此次方案
                for(int i = 0 ; i <= antCount[0] ; ++i)
                    ant[i].reset();         //重置蚂蚁数据
                ant[0].addPlace(0);

                reset();                    //重置地图信息

                continue ;
            }


            for(int i = 1 ; i <= antCount[0] ; ++i){
//                tempLength += ant[i].length;
                if(ant[i].used != 0){
                    ArrayDeque<Double> temp = ant[i].remainingRoom.clone();
                    while(!temp.isEmpty()){
                        tempRemainingRoom += temp.removeFirst();
                    }
                }
            }


//            System.out.println("路径长度："+tempLength);
//                for(int i = 1 ; i <= tempRealCount ; ++i){
//
//                    StringBuffer buffer = new StringBuffer();
//                    buffer.append("蚂蚁:"+i+" 车型" + ant[i].getCapacity() + "：");
//
//                    ArrayDeque<Integer> temp = ant[i].tabu.clone();
//                    while(!temp.isEmpty()){
//
//                        buffer.append(Map.cc[temp.peekFirst()].name);
//                        //buffer.append(temp.peekFirst());
//                        temp.removeFirst();
//
//                        if(!temp.isEmpty())
//                            buffer.append("->");
//                    }
//                    System.out.print(" ");
//
//                    ArrayDeque<Double> temp2 = ant[i].remainingRoom.clone();
//
//                    while(!temp2.isEmpty()){
//                        buffer.append(" "+ "满载率：" + (temp2.removeFirst() * 100) + "%");
//                    }
//
//                    String temp3 = buffer.toString();
//
//                    System.out.println(temp3);
//                }




            double tempMethod = tempLength * Ant.beta + (tempRemainingRoom * Ant.gamma) * tempRealCount;      //长度和满载率临时综合值
            System.out.println("tempMethod="+tempMethod);
            if(tempMethod < bestMethod){
                realAntCount = tempRealCount;
                count = 0;
                rou += 0.1;                    //如果产生了更短的路径，则减慢信息素的挥发，获得更多的启发信息
                if(rou >= 0.9)
                    rou = 0.9;

                bestLength = tempLength;
                bestMethod = tempMethod;              //记录更优的方案

                updateTabu();                           //更新最优方案
            }
            else
                count++;

            if(count >= 2){                 //如果连续两代都没有产生更好的路径，则加快信息素的挥发，减少信息素的作用
                //rou = 0.9 * rou * (count - 1);
                rou -= 0.1;
                if(rou <= 0.1)
                    rou = 0.1;
            }

            updateTrial();              //更新信息素

            reset();                    //重置地图信息

            for(int i = 0 ; i <= antCount[0] ; ++i)
                ant[i].reset();         //重置蚂蚁数据
            ant[0].addPlace(0);

            itorBestLength[max] = tempLength;       //构造折线图用

            max++;                      //当前迭代次数+1
        }
    }

    //更新最优方案
    public void updateTabu(){
        bestTabu.clear();                       //清空之前的最优方案

        for(int i = 1 ; i <= antCount[0] ; ++i){
            if(ant[i].used != 0){
                StringBuffer buffer = new StringBuffer();
                buffer.append("蚂蚁:"+i+" 车型" + ant[i].getCapacity() + "：");

                ArrayDeque<Integer> temp = ant[i].tabu.clone();
                while(!temp.isEmpty()){

                    buffer.append(Map.cc[temp.peekFirst()].name);
//                buffer.append(temp.peekFirst());
                    temp.removeFirst();

                    if(!temp.isEmpty())
                        buffer.append("->");
                }

                while(!ant[i].remainingRoom.isEmpty()){
                    buffer.append(" "+ "满载率：" + (ant[i].remainingRoom.removeFirst() * 100) + "%");
                }

                String temp2 = buffer.toString();

                bestTabu.add(temp2);
            }
        }
    }

    //更新地图上的信息素         与c++ant源代码的思路不同信息素的更新方式不同
    private void updateTrial(){
        for(int i = 0 ; i < antCount[0] ; ++i){
            ArrayDeque<Integer> temp = ant[i].tabu.clone();
            while(!temp.isEmpty()){
                int from = temp.peekFirst();
                temp.remove();
                if(temp.isEmpty())
                    break;
                int to = temp.peekFirst();

                //Map.pheromonesChange[from][to] = ant[i].antProduct(from, to);
                Map.pheromonesChange[from][to] = 100 / Map.distance[from][to];
                Map.pheromonesChange[to][from] = Map.pheromonesChange[from][to];
            }
        }

        for(int i = 0 ; i < Map.clientCount + 1 ; ++i){
            for(int j = 0 ; j < Map.clientCount + 1 ; ++j){
                Map.pheromones[i][j] = (rou * Map.pheromones[i][j]) + Map.pheromonesChange[i][j];
                Map.pheromonesChange[i][j] = 0;
            }
        }
    }

    //计算运送时间
    public void calculationCarryTime(){         //记得统一速度和时间的单位
        carryTime = bestLength / Ant.pace;

        carryTime += Map.unloadTime * Map.clientCount;
    }

    //遗传算法的输出函数
    public void out(){
        System.out.println("最优路径长度为：" + bestLength);
        System.out.println("路径顺序为:");
        for(int i = 0 ; i < bestTabu.size() ; ++i)
            System.out.println(bestTabu.get(i));

        System.out.println("运送时间为：" + getCarryTime());
    }
}
