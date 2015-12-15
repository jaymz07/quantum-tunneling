import java.util.*;
import java.io.*;
import java.applet.*;
import java.awt.*;

public class multiGraph
{
	public int WIDTH=1000;
	public int HEIGHT=600;
	public ArrayList<DataSet> points;
	public ArrayList<DataSet> sPoints;
	public Color color=Color.RED;
	public int pSize=1;
	public double ppuX=0, ppuY=0, rangeX=0, rangeY=0, minX, minY, maxX, maxY;

	Graphics page;

	public multiGraph(Graphics b, ArrayList<DataSet> a, int w, int h)
	{
		WIDTH=w;
		HEIGHT=h;
		points=a;
		page=b;

		minX=points.get(0).data.get(0).getX();
		minY=points.get(0).data.get(0).getY();
		maxX=minX;
		maxY=minY;

		for(int i=0;i<points.size();i++)
		{
			minX=Math.min(minX,points.get(i).minX());
			minY=Math.min(minY,points.get(i).minY());
			maxX=Math.max(maxX,points.get(i).maxX());
			maxY=Math.max(maxY,points.get(i).maxY());
		}

		rangeX=maxX-minX;
		rangeY=maxY-minY;
		ppuY=(WIDTH)/(rangeX);
		ppuX=(HEIGHT)/(rangeY);

		sPoints=new ArrayList<DataSet>();

		for(int i=0;i<points.size();i++)
		{
			ArrayList<Point> pts= new ArrayList<Point>();
			for(int j=0;j<points.get(i).data.size();j++)
			{
				pts.add(scrPoint(points.get(i).data.get(j)));
			}
			sPoints.add(new DataSet(pts));
		}

		color=Color.BLUE;
	}

	public void printGraph()
	{
		page.setColor(Color.WHITE);
	//	page.fillRect(0,0,WIDTH,HEIGHT);
		//page.setColor(color);
		for(int i=0;i<sPoints.size();i++)
		{
		//	page.setColor(new Color(255-255/sPoints.size()*(i+1),0,255/sPoints.size()*(i+1)));
			page.setColor(new Color(getColorValue(i,0,sPoints.size())));
			int x=(int)sPoints.get(i).getData().get(0).getX(), y=(int)sPoints.get(i).getData().get(0).getY();

			for(Point p : sPoints.get(i).getData())
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

	/*public void printGraph(Point p, int w, int h)
	{
		WIDTH=w;
		HEIGHT=h;
		Graph g = new Graph(page,points,w,h);
		sPoints=g.sPoints;
		for(Point pt : sPoints.getData())
		{
			pt.y+=p.y;
			pt.x+=p.x;
		}

		printGraph();

	}*/

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