import java.util.ArrayDeque;
import java.util.ArrayList;

public class AntProject {

    Map map;                                  //地图类
    public static int count = 0;              //连续多少代没有产生更优解
    private final int antCount = Main.carCount;           //蚂蚁的数量
    private int realAntCount = antCount;                    //实际使用的车辆数量
    public static double rou = 0.5;            //信息素的挥发系数
    private final int itCount = 500;           //最大迭代次数

    public int getItCount(){        //用于曲线图
        return itCount;
    }           //构造曲线图用

    private double bestMethod;                 //记录最佳方案的综合值
    private double bestLength;                  //记录最优方案的路径长度
    private ArrayList<String> bestTabu;           //记录最佳方案
    private double carryTime;                     //运送时间
    private Ant ant[];

    public double itorBestLength[];             //记录每次迭代后的最佳路径长度        构造曲线图用

    //构造函数
    public AntProject(){
        map = new Map();
        initMap();                              //初始化地图
        bestMethod = Double.MAX_VALUE;
        bestLength = Double.MAX_VALUE;
        bestTabu = new ArrayList<>();
        ant = new Ant[antCount];
        for(int i = 0 ; i < antCount ; ++i)
            ant[i] = new Ant();

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
        return antCount;
    }

    //获取运送时间
    public double getCarryTime(){
        return carryTime;
    }

    //设置每辆车的承载量
    public void setAllAntCapacity(){
        for(int i = 0 ; i < antCount ; ++i)
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
        for(int i = 0 ; i < antCount ; ++i){                    //将配送中心设置为每只蚂蚁的初始点
            //ant[i].resetRoom();
            ant[i].addPlace(0);
        }
    }

    //迭代搜索
    public void startSearch(){
        int max = 0;                                //当前的代数
        int tempRealCount = antCount;

        while(max < itCount){
            double tempLength = 0;
            double tempRemainingRoom = 0;       //每次路线的满载率之和
            boolean isOnceComplete = true;      //蚁群循环中是否只执行一次

            Beyond:
            while(!Map.end()){
                for(int i = 0 ; i < antCount ; ++i){
                    while(ant[i].getAllowChoseNextCity()){
                        tempLength += ant[i].mov();

                        if(ant[i].getDestroy()){
                            while (ant[i].path.size() > 1){
                                int to = ant[i].path.peekLast();
                                ant[i].path.removeLast();
                                if(ant[i].path.isEmpty())
                                    break;
                                int from = ant[i].path.peekLast();

                                tempLength -= Map.distance[from][to];
                                ant[i].length -= Map.distance[from][to];
                                if(ant[i].tabu.size() > 1)
                                    ant[i].tabu.removeLast();
                            }
                        }

                        if(ant[i].path.peekLast() == 0){
                            ant[i].path.clear();
                            ant[i].path.addLast(0);
                        }

                        if(ant[i].getDestroy()){
                            ant[i].setDestroy(false);
                            continue ;
                        }



                        if(ant[i].tabu.peekLast() == 0 && ant[i].tabu.size() != 1)
                            ant[i].setAllowChoseNextCity(false);

                        if(tempLength > (5 * bestLength)){
                            break ;
                            //如果此次方案长度太长则舍弃此次方案
                        }
                    }

                    ant[i].remainingRoom.add((ant[i].getCapacity() - ant[i].getRoom()) / ant[i].getCapacity());

                    ant[i].resetGo();

                    if(tempLength > (5 * bestLength)){
                        //如果此次方案长度太长则舍弃此次方案
                        break Beyond;
                    }

                    if(isOnceComplete == false){
                        if(Map.end())
                            break;
                        else
                            continue;
                    }

                    if(Map.end()){
                        tempRealCount = i + 1;
                        break;
                    }
                }

                isOnceComplete = false;
            }

            if(tempLength > (5 * bestLength)){                    //如果此次方案长度太长则舍弃此次方案
                for(int i = 0 ; i < antCount ; ++i)
                    ant[i].reset();         //重置蚂蚁数据

                reset();                    //重置地图信息

                continue ;
            }


            for(int i = 0 ; i < antCount ; ++i){
//                tempLength += ant[i].length;

                ArrayDeque<Double> temp = ant[i].remainingRoom.clone();
                while(!temp.isEmpty()){
                    tempRemainingRoom += temp.removeFirst();
                }
            }



//                for(int i = 0 ; i < realAntCount ; ++i){
//
//                    StringBuffer buffer = new StringBuffer();
//                    buffer.append("蚂蚁:"+i+" 车型" + ant[i].getCapacity() + "：");
//
//                    ArrayDeque<Integer> temp = ant[i].tabu.clone();
//                    while(!temp.isEmpty()){
//
//                        //buffer.append(Map.cc[temp.peekFirst()].name);
//                        buffer.append(temp.peekFirst());
//                        temp.removeFirst();
//
//                        if(!temp.isEmpty())
//                            buffer.append("->");
//                    }
//                    System.out.print(" ");
//
//                    while(!ant[i].remainingRoom.isEmpty()){
//                        buffer.append(" "+ "满载率：" + (ant[i].remainingRoom.removeFirst() * 100) + "%");
//                    }
//
//                    String temp2 = buffer.toString();
//
//                    System.out.println(temp2);
//                }




            double tempMethod = tempLength * Ant.beta + tempRemainingRoom * tempRealCount * Ant.gamma;      //长度和满载率临时综合值
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

            for(int i = 0 ; i < antCount ; ++i)
                ant[i].reset();         //重置蚂蚁数据

            itorBestLength[max] = tempLength;       //构造折线图用

            max++;                      //当前迭代次数+1
        }
    }

    //更新最优方案
    public void updateTabu(){
        bestTabu.clear();                       //清空之前的最优方案

        for(int i = 0 ; i < realAntCount ; ++i){

            StringBuffer buffer = new StringBuffer();
            buffer.append("蚂蚁:"+i+" 车型" + ant[i].getCapacity() + "：");

            ArrayDeque<Integer> temp = ant[i].tabu.clone();
            while(!temp.isEmpty()){

                //buffer.append(Map.cc[temp.peekFirst()].name);
                buffer.append(temp.peekFirst());
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

    //更新地图上的信息素         与c++ant源代码的思路不同信息素的更新方式不同
    private void updateTrial(){
        for(int i = 0 ; i < antCount ; ++i){
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
