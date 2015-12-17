import java.io.*;
import java.util.*;

public class DataSet
{
	public ArrayList<Point> data = new ArrayList<Point>();


	public DataSet(ArrayList<Point> a)
	{
		data =a;
	}
	public DataSet(Point [] a) {
	    data = new ArrayList<Point>();
	    for(Point p : a)
	      data.add(p);
	}
	public DataSet(double [] xpoints, double [] ypoints) {
	  for(int i=0;i< xpoints.length; i++)
	    data.add(new Point(xpoints[i],ypoints[i]));
	}

	public DataSet(ArrayList<Point> a,int p1, int p2)
	{
		this();
		for(int i=p1;i<=p2;i++)
			if(i>=0&&i<a.size())
				data.add(a.get(i));

	}

	public DataSet()
	{
		data= new ArrayList<Point>();
	}

	public ArrayList<Point> getData()
	{
		return data;
	}

	public void setData(ArrayList<Point> a)
	{
		data=a;
	}

	public double minY()
	{
		double temp=data.get(0).getY();
		for(int i=0;i<data.size();i++)
			if(temp>data.get(i).getY()&&data.get(i).getY()!=Double.NaN&&data.get(i).getY()!=Double.POSITIVE_INFINITY&&data.get(i).getY()!=Double.NEGATIVE_INFINITY)
				temp=data.get(i).getY();

		return temp;
	}

	public double maxY()
	{
		double temp=data.get(0).getY();
		for(int i=0;i<data.size();i++)
			if(temp<data.get(i).getY()&&data.get(i).getY()!=Double.NaN&&data.get(i).getY()!=Double.POSITIVE_INFINITY&&data.get(i).getY()!=Double.NEGATIVE_INFINITY)
				temp=data.get(i).getY();

		return temp;
	}

	public int minYI()
	{
		int index=0;
		double temp=data.get(0).getY();
		for(int i=0;i<data.size();i++)
			if(temp>data.get(i).getY()&&data.get(i).getY()!=Double.NaN&&data.get(i).getY()!=Double.POSITIVE_INFINITY&&data.get(i).getY()!=Double.NEGATIVE_INFINITY)
			{
				temp=data.get(i).getY();
				index=i;
			}


		return index;
	}

	public int maxYI()
	{
		int index=0;
		double temp=data.get(0).getY();
		for(int i=0;i<data.size();i++)
			if(temp<data.get(i).getY()&&data.get(i).getY()!=Double.NaN&&data.get(i).getY()!=Double.POSITIVE_INFINITY&&data.get(i).getY()!=Double.NEGATIVE_INFINITY)
			{
				temp=data.get(i).getY();
				index=i;
			}


		return index;
	}




	public double minX()
	{
		double temp=data.get(0).getX();
		for(int i=0;i<data.size();i++)
			if(temp>data.get(i).getX()&&data.get(i).getX()!=Double.NaN&&data.get(i).getX()!=Double.POSITIVE_INFINITY&&data.get(i).getX()!=Double.NEGATIVE_INFINITY)
				temp=data.get(i).getX();

		return temp;
	}

	public double maxX()
	{
		double temp=data.get(0).getX();
		for(int i=0;i<data.size();i++)
			if(temp<data.get(i).getX()&&data.get(i).getX()!=Double.NaN&&data.get(i).getX()!=Double.POSITIVE_INFINITY&&data.get(i).getX()!=Double.NEGATIVE_INFINITY)
				temp=data.get(i).getX();

		return temp;
	}

	public int minXI()
	{
		int index=0;
		double temp=data.get(0).getX();
		for(int i=0;i<data.size();i++)
			if(temp>data.get(i).getX()&&data.get(i).getX()!=Double.NaN&&data.get(i).getX()!=Double.POSITIVE_INFINITY&&data.get(i).getX()!=Double.NEGATIVE_INFINITY)
			{
				temp=data.get(i).getX();
				index=i;
			}


		return index;
	}

	public int maxXI()
	{
		int index=0;
		double temp=data.get(0).getX();
		for(int i=0;i<data.size();i++)
			if(temp<data.get(i).getX()&&data.get(i).getX()!=Double.NaN&&data.get(i).getX()!=Double.POSITIVE_INFINITY&&data.get(i).getX()!=Double.NEGATIVE_INFINITY)
			{
				temp=data.get(i).getX();
				index=i;
			}


		return index;
	}

	public double avgY()
	{
		int num=data.size();
		double out=0;
		for(int i=0;i<data.size();i++)
			if(!(data.get(i).getY()!=Double.NaN&&data.get(i).getY()!=Double.POSITIVE_INFINITY&&data.get(i).getY()!=Double.NEGATIVE_INFINITY))
				num--;
		for(int i=0;i<data.size();i++)
			if(data.get(i).getY()!=Double.NaN&&data.get(i).getY()!=Double.POSITIVE_INFINITY&&data.get(i).getY()!=Double.NEGATIVE_INFINITY)
				out+=data.get(i).y/num;
		return out;

	}

	public double avgYInRange(int p1, int p2)
	{
		int count=0;
		double avg=0;
		for(int i=p1;i<=p2;i++)
			if(i>=0&&i<data.size())
			{
				count++;
				avg+=data.get(i).y;
			}
		return avg/count;
	}
	public double avgYInRangeExcluding(int p1, int p2, int exclude)
	{
		int count=0;
		double avg=0;
		for(int i=p1;i<=p2;i++)
			if(i>=0&&i<data.size()&&i!=exclude)
			{
				count++;
				avg+=data.get(i).y;
			}
		return avg/count;
	}

	public int maxPointInRange(int p1, int p2)
	{
		double max=Double.MIN_VALUE;
		int maxIndex=0;
		for(int i=p1;i<=p2;i++)
		{
			if(i>=0&&i<data.size()&&data.get(i).y>max)
			{
				max=data.get(i).y;
				maxIndex=i;
			}
		}
		return maxIndex;
	}

	public double minYInRange(int p1, int p2)
	{
		double min=Double.MIN_VALUE;
		for(int i=p1;i<=p2;i++)
			if(i>=0&&i<data.size())
				min=Math.min(min,data.get(i).y);
		return min;
	}
	public Point minYPointInRange(int p1, int p2)
	{
		double min=Double.MIN_VALUE;
		int index=-1;
		for(int i=p1;i<=p2;i++)
			if(i>=0&&i<data.size()) {
				index=i;
				min=Math.min(min,data.get(i).y);
			}
		return data.get(index);
	}
	public void scaleY(double s)
	{
		for(Point p : data)
			p.y*=s;
	}
	public void copyData(ArrayList<Point> pts)
	{
		data=new ArrayList<Point>();
		if(pts==null) {
			data=null;
			return;
		}
		for(Point p : pts)
			data.add(new Point(p.x,p.y));
	}
	public void copyData(ArrayList<Point> pts,int r1, int r2)
	{
		data=new ArrayList<Point>();
		if(r1>r2)
		{
			int temp=r1;
			r1=r2;
			r2=temp;
		}
		for(int i=r1;i<r2;i++)
			if(i>=0&&i<pts.size())
				data.add(new Point(pts.get(i).x,pts.get(i).y));
	}
	public void copyYData(ArrayList<Point> pts)
	{
		data=new ArrayList<Point>();
		int count=0;
		for(Point p : pts) {
			data.add(new Point(count,p.y));
			count++;
		}
	}

	public double rangeX()
	{
		return maxX()-minX();
	}

	public double rangeY()
	{
		return maxY()-minY();
	}

	public void add(Point a)
	{
		data.add(a);
	}
	public double integrate()
	{
		double out=0;
		for(int i=1;i<data.size();i++)
			out+=(data.get(i).x-data.get(i-1).x)*(data.get(i).y+data.get(i-1).y)/2;
		return out;
	}
	public double integrate(int xp)
	{
		double out=0;
		for(int i=1;i<xp;i++)
			out+=(data.get(i).x-data.get(i-1).x)*(data.get(i).y+data.get(i-1).y)/2;
		return out;
	}
	public double simpIntegrate()
	{
		double out=0;
		for(int i=0;i<data.size();i++)
		{
			double fx=data.get(i).y;
			if(i==0||i==data.size()-1)
				out+=fx;
			else if(i%2==0)
				out+=4*fx;
			else if(i%2==1)
				out+=2*fx;
		}
		out*=(data.get(1).x-data.get(0).x)/(3);
		return out;
	}
	public double simpIntegrate(int a, int b)
	{
		double out=0;
		int multiplier=1;
		if(a==b)
			return 0;
		if(a>b)
		{
			int c=b;
			b=a;
			a=c;
			multiplier=-1;
		}
		for(int i=a;i<=b;i++)
		{
			double fx=data.get(i).y;
			if(i==0||i==data.size()-1)
				out+=fx;
			else if(i%2==0)
				out+=4*fx;
			else if(i%2==1)
				out+=2*fx;
		}
		out*=(data.get(1).x-data.get(0).x)/(3);
		return out*multiplier;
	}
	public double simpIntegrate(double a, double b)
	{
		return simpIntegrate(getIndexAt(a),getIndexAt(b));
	}
	public DataSet getIntegral()
	{
		ArrayList<Point> out=new ArrayList<Point>();
		for(int i=1;i<data.size();i++)
			out.add(new Point(data.get(i).x,integrate(i)));
		return new DataSet(out);
	}
	public DataSet getDerivative()
	{
		ArrayList<Point> out=new ArrayList<Point>();
		for(int i=1;i<data.size();i++)
			out.add(new Point(data.get(i).x,(data.get(i).y-data.get(i-1).y)/(data.get(i).x-data.get(i-1).x)));
		return new DataSet(out);
	}
	public DataSet getSpectrum()
	{
		ArrayList<Point> points = data;
		int numHarmonics=data.size()/4;
		double period=rangeX();

		ArrayList<Point> out = new ArrayList<Point>();
		for(double i=1;i<numHarmonics;i+=1)
		{
			ArrayList<Point> a = new ArrayList<Point>();
			ArrayList<Point> b = new ArrayList<Point>();
			for(Point pt : points)
			{
				double w=Math.PI*2/period;
				a.add(new Point(pt.x,pt.y*Math.cos(i*w*pt.x)));
				b.add(new Point(pt.x,pt.y*Math.sin(i*w*pt.x)));
			}
			double an=(new DataSet(a)).simpIntegrate(),bn=(new DataSet(b)).simpIntegrate();
			out.add(new Point(out.size(),an));
			out.add(new Point(out.size(),bn));
		}
		return new DataSet(out);

	}
	public DataSet getSpectrum(int s, int e)
	{
		ArrayList<Point> points = data;
		int numHarmonics=e;
		double period=0;

		period=points.get(points.size()-1).x-points.get(0).x;

		ArrayList<Point> out = new ArrayList<Point>();
		for(double i=s;i<numHarmonics;i+=1)
		{
			ArrayList<Point> a = new ArrayList<Point>();
			ArrayList<Point> b = new ArrayList<Point>();
			for(Point pt : points)
			{
				double w=Math.PI*2/period;
				a.add(new Point(pt.x,pt.y*Math.cos(i*w*pt.x)));
				b.add(new Point(pt.x,pt.y*Math.sin(i*w*pt.x)));
			}
			double an=(new DataSet(a)).integrate(),bn=(new DataSet(b)).integrate();
			out.add(new Point(i,an+bn));
		}
		return new DataSet(out);

	}
	public int getIndexAt(double xPos)
	{
	  double min=Double.MAX_VALUE;
	  int ind=-1;
	  for(int i=0;i<data.size();i++)
	  {
	    double dist=Math.abs(data.get(i).x-xPos);
	    if(dist<min)
	    {
	      min=dist;
	      ind=i;
	    }
	  }
	  return ind;
	}
	public double interpolate(double xPoint)
	{
	  int closest1=-1,closest2=-1;
	  double min1=Double.MAX_VALUE,min2=Double.MAX_VALUE;
	  for(int i=0;i<data.size();i++)
	  {
	  	double dist=Math.abs(data.get(i).x-xPoint);
	  	if(min1>dist) {
	  		min2=min1;
	  		min1=dist;
	  		closest2=closest1;
	  		closest1=i;
	  	}
	  	else if(min2>dist) {
	  		min2=dist;
	  		closest2=i;
	  	}
	  }
	  return data.get(closest1).y+(data.get(closest2).y-data.get(closest1).y)/(data.get(closest2).x-data.get(closest1).x)*(xPoint-data.get(closest1).x);
	}


}