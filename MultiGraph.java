import java.util.*;
import java.io.*;
import java.applet.*;
import java.awt.*;
import javax.swing.*;

public class MultiGraph
{
    //--Default parameters---------------
    public int pSize=1;
    public double margin = 0.1;
    public double legendOpacity = 0.85, legendMargin = 0.05;
    public int displayDigits = 1;
    public String title = "";
    public boolean showXTicks = true, showYTicks = true;
    //------------------------------------

    //Internal stuff
    public int WIDTH;
    public int HEIGHT;
    public double minX, minY, maxX, maxY;
    public ArrayList<Graph> graphs;

    private ArrayList<ArrayList<Point>>  sPoints = null;
    private double tickIncrement = 0.1;
    private double rangeX=0, rangeY=0, ppuX=0, ppuY=0;
    private Graphics page;
    public ArrayList<Color> 	colors = null;
    public ArrayList<Double> 	pointSizes = null;

    private enum Axis { X, Y }

    public MultiGraph(ArrayList<Graph> graphList) {
        graphs=graphList;

        findBounds();
        setPlotParams();
        maxX += rangeX*margin;
        minX -= rangeX*margin;
        maxY += rangeY*margin;
        minY -= rangeY*margin;
        setPlotParams();
    }

    //------------Object builder patterns-------------
    public MultiGraph setTitle(String plotTitle) {
        title = plotTitle;
        return this;
    }
    public MultiGraph setMaxX(double max_X) {
        maxX = max_X;
        sPoints = null;
        return this;
    }
    public MultiGraph setMaxY(double max_Y) {
        maxY = max_Y;
        sPoints = null;
        return this;
    }
    public MultiGraph setMinX(double min_X) {
        minX = min_X;
        sPoints = null;
        return this;
    }
    public MultiGraph setMinY(double min_Y) {
        minY = min_Y;
        sPoints = null;
        return this;
    }

    public void findBounds() {
        //Find bounds of data and set that as default plot range
        minX=graphs.get(0).data.get(0).getX();
        minY=graphs.get(0).data.get(0).getY();
        maxX=minX;
        maxY=minY;

        for(int i=0; i<graphs.size(); i++) {
            ArrayList<Point> graphData = graphs.get(i).data;
            for(int j=0; j<graphData.size(); j++) {
                minX=Math.min(minX,graphData.get(j).x);
                minY=Math.min(minY,graphData.get(j).y);
                maxX=Math.max(maxX,graphData.get(j).x);
                maxY=Math.max(maxY,graphData.get(j).y);
            }
        }
    }

    public void setColors() {
        int numUncoloredGraphs = 0, count = 0;
        for(Graph g : graphs)
            if(g.color == null)
                numUncoloredGraphs ++;
        ArrayList<Color> colorsOut = new ArrayList<Color>();
        for(Graph g : graphs) {
            if(g.color == null) {
                colorsOut.add(getColorValue(count,0,numUncoloredGraphs));
                count++;
            }
            else
                colorsOut.add(g.color);
        }
        colors = colorsOut;
    }

    public void generateScreenPoints()
    {
        setPlotParams();
        sPoints=new ArrayList<ArrayList<Point>>();

        for(int i=0; i<graphs.size(); i++)
        {
            ArrayList<Point> pts= new ArrayList<Point>();
            for(int j=0; j<graphs.get(i).data.size(); j++)
                pts.add(scrPoint(graphs.get(i).data.get(j)));
            sPoints.add(pts);
        }
    }
    public void printGraph(Graphics graphics, int w, int h)
    {
        WIDTH = w;
        HEIGHT =h;
        setPlotParams();
        page = graphics;

        if(sPoints == null)
            generateScreenPoints();

        if(colors == null)
            setColors();

        //page.setColor(Color.WHITE);
        //page.fillRect(0,0,WIDTH,HEIGHT);

        for(int i=0; i<sPoints.size(); i++)
        {
            page.setColor(colors.get(i));

            if(!graphs.get(i).shaded) {
                int x=(int)sPoints.get(i).get(0).getX(), y=(int)sPoints.get(i).get(0).getY();
                pSize = graphs.get(i).pointSize;

                for(int j =0; j<sPoints.get(i).size(); j++)
                {
                    Point p = sPoints.get(i).get(j);
                    if(pSize>1)
                        drawPlotPoint((int)p.x, (int)p.y, graphs.get(i).pointStyle, pSize);
                    page.drawLine(x,y,(int)p.getX(),(int)p.getY());
                    x=(int)p.getX();
                    y=(int)p.getY();
                }
            }
            else {
                int [] xPoints = new int[sPoints.get(i).size()], yPoints = new int[sPoints.get(i).size()];
                for(int j =0; j<sPoints.get(i).size(); j++) {
                    xPoints[j] = (int)sPoints.get(i).get(j).x;
                    yPoints[j] = (int)sPoints.get(i).get(j).y;
                }
                page.fillPolygon(xPoints,yPoints,sPoints.size());
            }
        }
        drawAxes();
        drawLegend();
    }

    private void setPlotParams() {
        rangeX=maxX-minX;
        rangeY=maxY-minY;
        ppuY=(WIDTH)/(rangeX);
        ppuX=(HEIGHT)/(rangeY);
    }

    public Point scrPoint(Point p)
    {
        return new Point(((p.getX()-minX))*ppuY,HEIGHT-(p.getY()-minY)*ppuX);
    }
    public Color getColorValue(double in, double min, double max)
    {
        float val=(float)(Math.pow((in-min),1)/Math.pow((max-min),1));
        Color c = Color.getHSBColor(val,1.0f,0.75f);
        return new Color(c.getRGB());
    }
    private ArrayList<Double> generateTickLocs(Axis a, double tickInc) {
        ArrayList<Double> out = new ArrayList<Double>();

        int numDecPlaces = 0;
        double msDigits = 0, firstTick = 0, lower= 0, upper =0;

        switch(a) {
        case X:
            numDecPlaces = (int)(Math.log10(rangeX));
            msDigits = rangeX/Math.pow(10,numDecPlaces);
            lower = minX;
            upper = maxX;
            break;
        case Y:
            numDecPlaces = (int)(Math.log10(rangeY));
            msDigits = rangeY/Math.pow(10,numDecPlaces);
            lower = minY;
            upper = maxY;
            break;
        }
        if(upper-lower < 1)
            numDecPlaces --;
        double tenbase = Math.pow(10.0,numDecPlaces);
        for(double tick=0; tick<=1.0; tick+=tickInc) {
            double val = tenbase*tick*10;
            if(lower >= 0)
                val += (int)(lower/tenbase)*tenbase;
            else
                val += ((int)(lower/tenbase) - 1)*tenbase;
            if(val >=lower && val <= upper)
                out.add(val);
        }
        //System.out.println(numDecPlaces +"");
        return out;
    }
    public String dispNum(double num, int digits) {
        //return ((int)(num*Math.pow(10,digits)))/Math.pow(10.0,digits) + "";
        if(Math.abs(num) < Math.pow(10,-digits+1))
            return String.format("%."+digits+"e%n",num);
        return String.format("%."+digits+"f%n",num);
    }
    public void drawAxes() {
        int tickWidth = 10;
        page.setColor(Color.BLACK);
        if(minX<0&&maxX>0)
        {
            Point a = scrPoint(new Point(0,maxY));
            Point b = scrPoint(new Point(0,minY));
            page.drawLine((int)a.getX(),(int)a.getY(),(int)b.getX(),(int)b.getY());

            if(showYTicks) {
                ArrayList<Point> scrPoints = new ArrayList<Point>();
                ArrayList<Double> vals = generateTickLocs(Axis.Y,tickIncrement);
                if(vals.size() < 4)
                    vals = generateTickLocs(Axis.Y,tickIncrement/4);
                for(double tick :  vals) {
                    Point coordPoint = scrPoint(new Point(0,tick));
                    coordPoint.x += tickWidth;
                    scrPoints.add(coordPoint);
                    coordPoint = scrPoint(new Point(0,tick));
                    coordPoint.x -= tickWidth;
                    scrPoints.add(coordPoint);
                }
                for(int i =0; i<scrPoints.size(); i+=2) {
                    Point p1 = scrPoints.get(i), p2 = scrPoints.get(i+1);
                    page.drawLine((int)p1.getX(),(int)p1.getY(),(int)p2.getX(),(int)p2.getY());
                    page.drawString(dispNum(vals.get(i/2),displayDigits),(int)p1.getX() + tickWidth,(int)p1.getY());
                }
            }
        }
        if(minY<0&&maxY>0)
        {
            Point a = scrPoint(new Point(maxX,0));
            Point b = scrPoint(new Point(minX,0));
            page.drawLine((int)a.getX(),(int)a.getY(),(int)b.getX(),(int)b.getY());

            if(showXTicks) {
                ArrayList<Point> scrPoints = new ArrayList<Point>();
                ArrayList<Double> vals = generateTickLocs(Axis.X,tickIncrement);
                if(vals.size() < 3)
                    vals = generateTickLocs(Axis.X,tickIncrement/4);
                for(double tick : vals ) {
                    Point coordPoint = scrPoint(new Point(tick,0));
                    coordPoint.y += tickWidth;
                    scrPoints.add(coordPoint);
                    coordPoint = scrPoint(new Point(tick,0));
                    coordPoint.y -= tickWidth;
                    scrPoints.add(coordPoint);
                }
                for(int i =0; i<scrPoints.size(); i+=2) {
                    Point p1 = scrPoints.get(i), p2 = scrPoints.get(i+1);
                    page.drawLine((int)p1.getX(),(int)p1.getY(),(int)p2.getX(),(int)p2.getY());
                    page.drawString(dispNum(vals.get(i/2),displayDigits),(int)p1.getX(),(int)p1.getY() + tickWidth);
                }
            }
        }
    }

    public void drawLegend() {
        double width = 0.0, height = 0.0;
        FontMetrics font = page.getFontMetrics();
        for(int i =0; i<graphs.size(); i++) {
            width = Math.max(width, font.getStringBounds(graphs.get(i).title,page).getWidth());
            height = font.getStringBounds(graphs.get(i).title,page).getHeight();
        }
        height += 7;
        int xPos = (int)((1.0-legendMargin)*WIDTH), yPos = (int)(legendMargin*HEIGHT);
        page.setColor(new Color(255,255,255,(int)(legendOpacity*255)));
        page.fillRect(xPos - (int)width - 30,yPos,(int)width + 30,(int)height* (graphs.size()+1));
        page.setColor(Color.BLACK);
        page.drawRect(xPos - (int)width - 30,yPos,(int)width + 30,(int)height* (graphs.size()+1));
        for(int i =0; i<graphs.size(); i++) {
            page.setColor(colors.get(i));
            page.drawLine(xPos - (int)width - 30 + 5, yPos + 7 + (int)(height*(i+0.5)), xPos - (int)width - 5, yPos + 7 + (int)(height*(i+0.5)));
            drawPlotPoint(xPos - (int)width - 15, yPos + 7 + (int)(height*(i+0.5)), graphs.get(i).pointStyle, graphs.get(i).pointSize);
            page.setColor(Color.BLACK);
            page.drawString(graphs.get(i).title, xPos - (int)width, yPos + 7 + (int)height*(i+1));
        }
    }

    private void drawPlotPoint(int x, int y, Graph.PointType type, int size) {
        switch(type) {
        case NONE:
            return;

        case BOX:
            page.fillRect(x-size/2, y-size/2, size, size);
            break;

        case DOT:
            page.fillOval(x-size/2, y-size/2, size, size);
            break;
        }
        return;
    }

    public JFrame plotFrame(int width, int height) {
        WIDTH = width;
        HEIGHT = height;
        JFrame plotFrame = new JFrame(title);
        plotFrame.setVisible(true);
        plotFrame.setResizable(false);
        plotFrame.setSize(width,height);
        plotFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DrawPanel drawPanel = new DrawPanel(width,height);
        plotFrame.getContentPane().add(drawPanel);

        plotFrame.repaint();

        return plotFrame;
    }

    class DrawPanel extends JPanel {
        private int width, height;
        public DrawPanel(int w, int h) {
            super();
            width = w;
            height = h;
        }
        public void paintComponent(Graphics g) {
            page = g;

            //Make White background
            page.setColor(Color.WHITE);
            page.fillRect(0,0,width,height);

            //------Plot graph object-------
            printGraph(page,height,height);
        }
    }
}
