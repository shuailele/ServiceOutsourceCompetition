import java.awt.BasicStroke;
import java.awt.Color;

import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

class Test2 {
    public static void main(String[] args) {
        // 注入数据1
        XYSeries linedataset1 = new XYSeries("线1");
        Main tc = new Main();
        double temp[] = tc.getItorBestLength();
        for(int i = 0 ; i < tc.getItCount() ; ++i){
            linedataset1.add(i, temp[i]);
        }

        //建立数据模型
        XYSeriesCollection localXYSeriesCollection = new XYSeriesCollection();
        localXYSeriesCollection.addSeries(linedataset1);

        XYSplineRenderer splinerenderer = new XYSplineRenderer();
        //设置线的笔触（粗细）
        splinerenderer.setSeriesStroke(0, new BasicStroke(2.0F, 1, 1, 1.0F));
        splinerenderer.setBaseShapesVisible(false);                     //将点隐藏
        //splinerenderer.setSeriesStroke(1, new BasicStroke(4.0F, 1, 1, 1.0F));

        splinerenderer.setPrecision(100);// 设置精度差（影响曲线弧度）


        //设置横纵坐标描述
        NumberAxis xAxis = new NumberAxis("迭代次数");
        xAxis.setAutoRangeIncludesZero(false);
        NumberAxis yAxis = new NumberAxis("长度");
        yAxis.setAutoRangeIncludesZero(false);

        XYPlot plot = new XYPlot(localXYSeriesCollection, xAxis, yAxis, splinerenderer);
        // x轴 // 分类轴网格是否可见
        plot.setDomainGridlinesVisible(true);
        // y轴 //数据轴网格是否可见
        plot.setRangeGridlinesVisible(true);
        // 是否显示格子线
        plot.setRangeGridlinesVisible(true);
        // 设置背景透明度
        plot.setBackgroundAlpha(0.3f);
        // 数据轴（y轴）色彩
        plot.setRangeGridlinePaint(Color.black);
        // 分类轴（x轴）色彩
        plot.setDomainGridlinePaint(Color.black);
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        //数据轴的数据标签（可以只显示整数标签，需要将AutoTickUnitSelection设false）
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        //是否强制在自动选择的数据范围中包含0
        rangeAxis.setAutoRangeIncludesZero(true);
        //设置坐标轴间距但必须满足一定条件
        rangeAxis.setUpperMargin(1);// Y轴间距
        rangeAxis.setLowerMargin(1);//X轴间距
        //坐标轴标题旋转角度（纵坐标可以旋转）
        rangeAxis.setLabelAngle(Math.PI / 2.0);

        JFreeChart chart = new JFreeChart("最短路径优化收敛过程", // 标题
                JFreeChart.DEFAULT_TITLE_FONT, // 标题的字体，这样就可以解决中文乱码的问题
                plot, true);

        ChartFrame pieFrame = new ChartFrame("统计图", chart);
        pieFrame.pack();
        pieFrame.setVisible(true);
        pieFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}