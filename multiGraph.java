import java.util.*;
import java.io.*;
import java.applet.*;
import java.awt.*;

public class multiGraph
{
    public int WIDTH=1000;
    public int HEIGHT=600;
    public ArrayList<ArrayList<Point>> 	points;
    public ArrayList<ArrayList<Point>> 	sPoints = null;
    public int pSize=1;
    public double minX, minY, maxX, maxY;

    private double rangeX=0, rangeY=0, ppuX=0, ppuY=0;
    private Graphics page;
    public ArrayList<Color> 	colors = null;
    public ArrayList<Integer> 	pointSizes = null;

    public multiGraph(Graphics b, ArrayList<ArrayList<Point>> a, int w, int h)
    {
        WIDTH=w;
        HEIGHT=h;
        points=a;
        page=b;

        //Find bounds of data and set that as default plot range
        minX=points.get(0).get(0).getX();
        minY=points.get(0).get(0).getY();
        maxX=minX;
        maxY=minY;

        for(int i=0; i<points.size(); i++)
            for(int j=0; j<points.get(i).size(); j++) {
                minX=Math.min(minX,points.get(i).get(j).x);
                minY=Math.min(minY,points.get(i).get(j).y);
                maxX=Math.max(maxX,points.get(i).get(j).x);
                maxY=Math.max(maxY,points.get(i).get(j).y);
            }
        setPlotParams();
    }
    //------------Object builder patterns-------------
    public multiGraph setMaxX(double max_X) {
        maxX = max_X;
	sPoints = null;
        return this;
    }
    public multiGraph setMaxY(double max_Y) {
        maxY = max_Y;
	sPoints = null;
        return this;
    }
    public multiGraph setMinX(double min_X) {
        minX = min_X;
	sPoints = null;
        return this;
    }
    public multiGraph setMinY(double min_Y) {
        minY = min_Y;
	sPoints = null;
        return this;
    }
    public multiGraph setColors(ArrayList<Color> colors) {
        this.colors = colors;
	sPoints = null;
        return this;
    }
    public multiGraph setSizes(ArrayList<Integer> pointSizes) {
        this.pointSizes = pointSizes;
	sPoints = null;
        return this;
    }

    public void generateScreenPoints()
    {
        setPlotParams();
        sPoints=new ArrayList<ArrayList<Point>>();

        for(int i=0; i<points.size(); i++)
        {
            ArrayList<Point> pts= new ArrayList<Point>();
            for(int j=0; j<points.get(i).size(); j++)
                pts.add(scrPoint(points.get(i).get(j)));
            sPoints.add(pts);
        }
    }
    public void printGraph()
    {
        if(sPoints == null)
            generateScreenPoints();
	else
	    setPlotParams();
        page.setColor(Color.WHITE);
        //	page.fillRect(0,0,WIDTH,HEIGHT);
        //page.setColor(color);
        for(int i=0; i<sPoints.size(); i++)
        {
            //	page.setColor(new Color(255-255/sPoints.size()*(i+1),0,255/sPoints.size()*(i+1)));
            page.setColor(new Color(getColorValue(i,0,sPoints.size())));
            int x=(int)sPoints.get(i).get(0).getX(), y=(int)sPoints.get(i).get(0).getY();

            for(Point p : sPoints.get(i))
            {

                page.fillRect((int)p.getX(),(int)p.getY(),pSize,pSize);
                page.drawLine(x,y,(int)p.getX(),(int)p.getY());
                x=(int)p.getX();
                y=(int)p.getY();
            }
        }

        page.setColor(Color.BLACK);

        if(minX<0&&maxX>0)
        {
            Point a = scrPoint(new Point(0,maxY));
            Point b = scrPoint(new Point(0,minY));
            page.drawLine((int)a.getX(),(int)a.getY(),(int)b.getX(),(int)b.getY());
        }
        if(minY<0&&maxY>0)
        {
            Point a = scrPoint(new Point(maxX,0));
            Point b = scrPoint(new Point(minX,0));
            page.drawLine((int)a.getX(),(int)a.getY(),(int)b.getX(),(int)b.getY());
        }
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
    public int getColorValue(double in, double min, double max)
    {
        float val=(float)(Math.pow((in-min),1)/Math.pow((max-min),1));
        Color c = Color.getHSBColor(val,1.0f,0.75f);
        return c.getRGB();
    }

}
