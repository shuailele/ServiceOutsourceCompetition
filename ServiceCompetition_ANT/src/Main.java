import java.util.Scanner;

public class Main {

    public static final int clientCount = inClientCount();              //客户数量
    public static final Client[] clientPlaces = inClientPlace();        //客户位置（点的位置）
    public static final double distance[][] = inDistance();             //输入客户间的距离（输入边）
    public static final int carType = inCarType();                      //输入车辆类型的数量
    public static final int []carCount = inCarCount();                    //输入车辆数量
    public static final double []carCapacity = inCarCapacity();           //输入车辆承载量
    public static final int unLoadTime = inUnLoadTime();                //输入卸货时间
    public static final int maxRemoving = inMaxRemoving();              //输入最大行驶距离
    public static final int pace = inPace();                            //输入车辆行驶速度

    AntProject project;

//    public static void main(String []args){
//        AntProject project = new AntProject();
//        project.setAllAntCapacity();
//        project.startSearch();                      //迭代搜索
//        System.out.println("迭代搜索完成");
//        project.calculationCarryTime();             //计算运送时间
//        project.out();                              //蚁群算法输出最短路径长度和路径顺序
//
//        //少一个计算使用了几辆车的函数然后在out函数中调用
//    }

    public Main(){
        project = new AntProject();
        //project.setAllAntCapacity();
        project.startSearch();                      //迭代搜索
        System.out.println("迭代搜索完成");
        project.calculationCarryTime();             //计算运送时间
        project.out();                              //蚁群算法输出最短路径长度和路径顺序
    }

    public  double[] getItorBestLength(){           //用于曲线图
        return project.itorBestLength;
    }

    public int getItCount(){            //用于曲线图
        return project.getItCount();
    }

    //用于在地图类中创建客户的数量
    public static int inClientCount(){
        System.out.println("请在下面分别输入客户数量、配送中心的名字、位置"+
                "、每个客户名字、位置、每个相联系的边、车辆数量、车辆承载量、卸货时间、车辆最大行驶里程、车辆速度");
        System.out.println();

        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入客户的数量:");

        int clientCount;
//        clientCount = scanner.nextInt();
        clientCount = 10;

        return clientCount;
    }

    //用于在地图类创建客户位置数组
    public static Client[] inClientPlace(){
        Scanner scanner = new Scanner(System.in);

        Client cc[] = new Client[clientCount + 1];

        for(int i = 0 ; i < clientCount + 1 ; ++i)
            cc[i] = new Client();

        System.out.println("请输入配送中心的名字:");
//        cc[0].name = scanner.next();
        cc[0].name = "P";
        System.out.println("请输入配送中心的横坐标和纵坐标:");
//        cc[0].x = scanner.nextDouble();
//        cc[0].y = scanner.nextDouble();
        cc[0].x = 0;
        cc[0].y = 0;

//        for(int i = 1 ; i < clientCount + 1 ; ++i){
//            System.out.println("请输入客户" + i + "的名字、横坐标、纵坐标和货物需求量");
//            cc[i].name = scanner.next();
//            cc[i].x = scanner.nextDouble();
//            cc[i].y = scanner.nextDouble();
//            cc[i].need = scanner.nextDouble();
//        }
        cc[1].need = 1.7;
        cc[2].need = 0.8;
        cc[3].need = 1.3;
        cc[4].need = 2.8;
        cc[5].need = 1.9;
        cc[6].need = 3.5;
        cc[7].need = 0.9;
        cc[8].need = 0.3;
        cc[9].need = 1.2;
        cc[10].need = 0;

        cc[1].name = "A";
        cc[2].name = "B";
        cc[3].name = "C";
        cc[4].name = "D";
        cc[5].name = "E";
        cc[6].name = "F";
        cc[7].name = "G";
        cc[8].name = "H";
        cc[9].name = "I";
        cc[10].name = "Z";

        return cc;
    }

    //用于在地图类中创建边矩阵
    public static double[][] inDistance(){
        Scanner scanner = new Scanner(System.in);
        double distance[][] = new double[clientCount + 1][clientCount + 1];

        System.out.println("请输入有联系的边的数量：");
        //int distanceCount = scanner.nextInt();
        int distanceCount = 22;

        System.out.println("请输入各个边:");

        int i = 0;
//        while(i++ < distanceCount){
//            System.out.println("请分别输入起始点、终点和路径长度");
//
//            int begin = scanner.nextInt();
//            if(begin > clientCount)
//                throw new IllegalArgumentException("输入的起始点数值过大");
//
//            int end = scanner.nextInt();
//            if(end > clientCount)
//                throw new IllegalArgumentException("输入的终点数值过大");
//
//            double weight = scanner.nextDouble();
//
//            distance[begin][end] = weight;
//            distance[end][begin] = weight;
//        }
        distance[0][1] = 5;
        distance[1][0] = 5;
        distance[0][2] = 8;
        distance[2][0] = 8;
        distance[0][3] = 7;
        distance[3][0] = 7;
        distance[0][5] = 4;
        distance[5][0] = 4;
        distance[0][6] = 12;
        distance[6][0] = 12;
        distance[0][7] = 9;
        distance[7][0] = 9;
        distance[0][8] = 12;
        distance[8][0] = 12;
        distance[0][9] = 6;
        distance[9][0] = 6;
        distance[0][10] = 5;
        distance[10][0] = 5;
        distance[1][2] = 4;
        distance[2][1] = 4;
        distance[2][3] = 3;
        distance[3][2] = 3;
        distance[3][4] = 4;
        distance[4][3] = 4;
        distance[4][5] = 3;
        distance[5][4] = 3;
        distance[5][6] = 10;
        distance[6][5] = 10;
        distance[6][7] = 4;
        distance[7][6] = 4;
        distance[6][8] = 7;
        distance[8][6] = 7;
        distance[7][8] = 5;
        distance[8][7] = 5;
        distance[8][9] = 9;
        distance[9][8] = 9;
        distance[9][1] = 3;
        distance[1][9] = 3;
        distance[3][10] = 5;
        distance[10][3] = 5;
        distance[4][10] = 2;
        distance[10][4] = 2;
        distance[5][10] = 2;
        distance[10][5] = 2;

        return distance;
    }

    //用于在地图类中规定每个点的卸货时间
    public static int inUnLoadTime(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入每个点的卸货时间：");
//        return scanner.nextInt();
        return 5;
    }

    //用于在蚂蚁类中规定每只蚂蚁行走的最大公里数
    public static int inMaxRemoving(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入车辆行驶的最大公里数：");
//        return scanner.nextInt();
        return 35;
    }

    //用于在蚂蚁类中规定每只蚂蚁行走速度
    public static int inPace(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入车辆的行驶速度：");
//        return scanner.nextInt();
        return 10;
    }

    //输入车辆类型的数量
    public static int inCarType(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入车辆类型的数量：");
        return scanner.nextInt();
//        return 1;
    }

    //用于在蚁群类中提供车辆数量
    public static int[] inCarCount(){
        int []carCount = new int[carType + 1];
        int allCarCount = 0;                //记录所有车辆的总数量
        Scanner scanner = new Scanner(System.in);
        for(int i = 1 ; i <= carType ; ++i){
            System.out.println("请输入车辆类型" + i + "的数量：");
            carCount[i] = scanner.nextInt();

            allCarCount += carCount[i];
        }

        carCount[0] = allCarCount;          //carCount0位置存放的是总车辆的数量

        return carCount;


//        int []arr = new int[2];
//        arr[0] = 10;
//        arr[1] = 10;
//        return arr;
    }

    //用于在蚁群类中提供车辆承载量
    public static double[] inCarCapacity(){
        double []carCapacity = new double[carType + 1];
        double maxCarCapacity = 0;          //记录所有车辆中的最大的承载量
        Scanner scanner = new Scanner(System.in);
        for (int i = 1 ; i <= carType ; ++i){
            System.out.println("请输入车辆类型" + i + "的承载量：");
            carCapacity[i] = scanner.nextDouble();

            if(carCapacity[i] > maxCarCapacity)
                maxCarCapacity = carCapacity[i];
        }
        carCapacity[0] = maxCarCapacity;        //carCapacity0位置存放的是所有车辆的最大的承载量
        return carCapacity;


//        double []arr = new double[2];
//        arr[0] = 5;
//        arr[1] = 5;
//        return arr;
    }

}
