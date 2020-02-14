import java.util.ArrayDeque;
import java.util.ArrayList;

public class AntProject {

    Map map;                                  //地图类
    public static int count = 0;              //连续多少代没有产生更优解
    private final int antCount = Main.carCount;           //蚂蚁的数量
    private int realAntCount = antCount;
    public static double rou = 0.5;            //信息素的挥发系数
    private final int itCount = 6000;           //最大迭代次数

    public int getItCount(){        //用于曲线图
        return itCount;
    }

    private double bestLength;                 //记录最佳路径长度
    private ArrayList<String> bestTabu;           //记录最佳方案
    private double carryTime;                     //运送时间
    private Ant ant[];

    public double itorBestLength[];             //记录每次迭代后的最佳路径长度        构造曲线图用

    //构造函数
    public AntProject(){
        map = new Map();
        initMap();                              //初始化地图
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

    //获取最佳路径长度
    public double getBestLength(){
        return bestLength;
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
            ant[i].resetRoom();
            ant[i].addPlace(0);
        }
    }

    //迭代搜索
    public void startSearch(){
        int max = 0;                                //当前的代数
        int tempRealCount = antCount;

        while(max < itCount){
            double tempLength = 0;
            while(!Map.end()){
                for(int i = 0 ; i < antCount ; ++i)
                    ant[i].setAllowChoseNextCity(true);

                for(int i = 0 ; i < antCount ; ++i){
                    while(ant[i].getAllowChoseNextCity()){
                        ant[i].mov();

                        if(ant[i].tabu.peekLast() == 0 && ant[i].tabu.size() != 1)
                            ant[i].setAllowChoseNextCity(false);
                    }

                    if(Map.end()){
                        tempRealCount = i + 1;
                        break;
                    }
                }
            }

            for(int i = 0 ; i < antCount ; ++i)
                tempLength += ant[i].length;

            if(tempLength < bestLength){
                realAntCount = tempRealCount;
                count = 0;
                rou += 0.1;                    //如果产生了更短的路径，则减慢信息素的挥发，获得更多的启发信息
                if(rou >= 0.9)
                    rou = 0.9;

                bestLength = tempLength;              //记录更优的路径长度

                updateTabu();                           //更新最优方案
            }
            else
                count++;

            if(count >= 2){                 //如果连续两代都没有产生更好的路径，则减慢信息素的挥发，减少信息素的作用
                rou -= 0.1;
                if(rou <= 0.1)
                    rou = 0.1;
            }

            updateTrial();              //更新信息素

            for(int i = 0 ; i < antCount ; ++i)
                ant[i].reset();         //重置蚂蚁数据

            reset();                    //重置地图信息

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

            double manzailv = (ant[i].getCapacity() - ant[i].getRoom()) / ant[i].getCapacity() * 100;
            buffer.append(" "+ "满载率：" + manzailv + "%");

            String temp2 = buffer.toString();

            bestTabu.add(temp2);
        }
    }

    //更新地图上的信息素         与c++ant源代码的思路不同信息素的更新方式不同
    private void updateTrial(){
        double pheromonesChange = 0;
        for(int i = 0 ; i < antCount ; ++i){
            ArrayDeque<Integer> temp = ant[i].tabu.clone();
            while(!temp.isEmpty()){
                int from = temp.peekFirst();
                temp.remove();
                if(temp.isEmpty())
                    break;
                int to = temp.peekFirst();

                pheromonesChange = ant[i].antProduct(from, to);
                Map.pheromonesChange[from][to] = ant[i].antProduct(from, to);
                Map.pheromonesChange[to][from] = Map.pheromonesChange[from][to];
            }
        }

        for(int i = 0 ; i < Map.clientCount + 1 ; ++i){
            for(int j = 0 ; j < Map.clientCount + 1 ; ++j){
                Map.pheromones[i][j] = (rou * Map.pheromones[i][j]) + pheromonesChange;
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
        System.out.println("最短路径长度为：" + bestLength);
        System.out.println("路径顺序为:");
        for(int i = 0 ; i < bestTabu.size() ; ++i)
            System.out.println(bestTabu.get(i));

        System.out.println("运送时间为：" + getCarryTime());
    }
}
