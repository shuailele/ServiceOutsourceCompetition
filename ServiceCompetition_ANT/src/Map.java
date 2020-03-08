public class Map {                  //地图类

    public static final int clientCount = Main.clientCount;                 //客户数量;
    public static final Client []cc = Main.clientPlaces;  //每个客户的位置
    public static final double distance[][] = Main.distance;//表示边 横坐标表示出发点 纵坐标表示到达点 矩阵中的值表示路径长度 0表示两点之间不存在边
    public static boolean ok[];                        //数组下标表示客户 数组中的值true表示已送达 false表示未送达
    public static double pheromones[][];           //信息素矩阵
    public static double pheromonesChange[][];     //信息素变化矩阵
    public static final int unloadTime = Main.unLoadTime;       //每个客户的卸货时间

    //构造函数
    public Map(){

        pheromones = new double[clientCount + 1][clientCount + 1];          //因为clientCount只包含客户数量不包含配送中心所以加1

        pheromonesChange = new double[clientCount + 1][clientCount + 1];

        ok = new boolean[clientCount + 1];

        for(int i = 1 ; i < clientCount + 1 ; ++i)  //将客户初始化为未送达  0位置表示配送中心
            ok[i] = false;
    }

    //判断地图中的客户是否全部配送完成
    public static boolean end(){
        for(int i = 1 ; i < clientCount + 1 ; ++i)
            if(ok[i] == false && cc[i].need != 0)
                return false;
        return true;
    }
}

class Client{              //城市类
    public double x;            //客户位置的横坐标
    public double y;            //客户位置的纵坐标
    public double need = 0.0;         //客户需要的货物数量
    public String name;         //客户的名字
}
