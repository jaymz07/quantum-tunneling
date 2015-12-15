import java.io.*;
import java.util.*;
import java.math.*;

public class Matrix
{
	public double [][] data;
	public Matrix(double [][] mat)
	{
		data = mat;
	}
	public Matrix(int n, char id)
	{
		data= new double[n][n];
		if(id=='i')
			for(int i=0;i<n;i++)
				data[i][i]=1;

	}
	public Matrix(Matrix mat)
	{
		data = new double[mat.data.length][mat.data[0].length];
		for(int i=0;i<data.length;i++)
			for(int j=0;j<data[0].length;j++)
				data[i][j]=mat.data[i][j];
	}
	public Matrix(int r, int c) //Random r X c matrix....
	{
		data=new double[r][c];
		for(int i=0;i<r;i++)
			for(int j=0;j<c;j++)
				data[i][j]=Math.random()*10;
	}
	public Matrix(double ax, double ay, double az) //Euler Rotation Matrix
	{
		double xs=Math.sin(ax),xc=Math.cos(ax),yc=Math.cos(ay),ys=Math.sin(ay),zs=Math.sin(az),zc=Math.cos(az);
		double [][] stuff={{yc*zc,-xc*zs+xs*ys*zc,xs*zs+xc*ys*zc},
							{yc*zs,xc*zc+xs*ys*zs,-xs*zc+xc*ys*zs},
							{-ys,xs*yc,xc*yc}};
		data=stuff;
	}
	public Matrix(double angle, char cord) //Coordinate Rotation Matrix
	{
		double s=Math.sin(angle),c=Math.cos(angle);
		if(cord=='x')
		{
			double[][] stuff = {{1,0,0},{0,c,-s},{0,s,c}};
			data=stuff;
		}
		else if(cord=='y')
		{
			double[][] stuff = {{c,0,s},{0,1,0},{-s,0,c}};
			data=stuff;
		}
		else if(cord=='z')
		{
			double[][] stuff = {{c,-s,0},{s,c,0},{0,0,1}};
			data=stuff;
		}

	}
	public Matrix(Point p)
	{
		data=new double[2][1];
		data[0][0]=p.x;
		data[1][0]=p.y;
	}
	public String toString()
	{
		String out="";
		for(int i=0;i<data.length;i++) {
			for(int j=0;j<data[0].length;j++)
				out=out+data[i][j]+"\t";
			out=out+"\n";
		}
		return out;
	}
	public String printP()
	{
		String out="{";
		for(int i=0;i<data.length;i++) {
			if(i>0)
				out=out+",";
			out=out+"{";
			for(int j=0;j<data[0].length;j++) {
				if(j>0)
					out=out+",";
				out=out+data[i][j];
			}
			out=out+"}";
		}
		out=out+"}";
		return out;
	}
	public double determinant()
	{
		int rows=data.length,cols=data[0].length,n=rows;
		if(rows!=cols)
			return Double.NaN;
		if(n==2)
		{
			return data[0][0]*data[1][1]-data[0][1]*data[1][0];
		}
		else
		{
			double out=0;
			for(int numC=0;numC<cols;numC++) {
				double [][] mat = new double[rows-1][cols-1];
				for(int i=1;i<rows;i++)
					for(int j=0;j<cols;j++)
						if(j!=numC){
							if(j<numC)
								mat[i-1][j]=data[i][j];
							else
								mat[i-1][j-1]=data[i][j];
						}
				if(numC%2==0)
					out+=data[0][numC]*(new Matrix(mat)).determinant();
				else
					out-=data[0][numC]*(new Matrix(mat)).determinant();
			}
			return out;
		}
	}
	public void multiply(double m)
	{
		for(int i=0;i<data.length;i++)
			for(int j=0;j<data[0].length;j++)
				data[i][j]*=m;
	}
	public Matrix times(double m)
	{
		Matrix out= new Matrix(this);
		out.multiply(m);
		return out;
	}
	public void add(Matrix m)
	{
		for(int i=0;i<data.length;i++)
			for(int j=0;j<data[0].length;j++)
				data[i][j]+=m.data[i][j];
	}
	public Matrix plus(Matrix m)
	{
		Matrix out = new Matrix(this);
		for(int i=0;i<data.length;i++)
			for(int j=0;j<data[0].length;j++)
				out.data[i][j]+=m.data[i][j];
		return out;
	}
	public Matrix inverse()
	{
		int rows=data.length,cols=data[0].length,n=rows;
		if(rows!=cols)
			return null;
		double [][] out = new double[n][n];
		for(int r=0;r<n;r++)
			for(int c=0;c<n;c++)
			{
				double [][] mat = new double [n-1][n-1];
				for(int i=0;i<n;i++)
					for(int j=0;j<n;j++)
						if(i!=c&&j!=r)
						{
							if(i<c)
							{
								if(j<r)
									mat[i][j]=data[i][j];
								else
									mat[i][j-1]=data[i][j];
							}
							else
							{
								if(j<r)
									mat[i-1][j]=data[i][j];
								else
									mat[i-1][j-1]=data[i][j];
							}
						}
				double det=(new Matrix(mat)).determinant();
				if((r-c)%2==0)
					out[r][c]=det;
				else
					out[r][c]=-det;

			}
		return (new Matrix(out)).times(1.0/determinant());
	}
	public Matrix multiply(Matrix m)
	{
		double [][] out = new double [data.length][m.data[0].length];
		for(int i=0;i<data.length;i++)
			for(int j=0;j<m.data[0].length;j++)
			{
				double term=0;
				for(int k=0;k<data[0].length;k++)
					term+=data[i][k]*m.data[k][j];
				out[i][j]=term;
			}
		return new Matrix(out);
	}
	public Matrix smoothData(int radius, double factor)
	{
		Matrix dat = new Matrix(this);
		for(int i=radius;i<data.length-radius;i++)
			for(int j=radius;j<data[0].length-radius;j++)
			{
				double val=0,sum=0;
				for(int k=-radius;k<=radius;k++)
					for(int l=-radius;l<=radius;l++)
					{
						val+=data[i+k][j+l]*data[i+k][j+l]*Math.exp(-(k*k+l*l)*factor);
						sum+=Math.exp(-(k*k+l*l)*factor);
					}
				dat.data[i][j]=Math.sqrt(val/sum);

			}
		return dat;
	}
	public Matrix transpose()
	{
		double [][] out = new double[data[0].length][data.length];
		for(int i=0;i<data.length;i++)
			for(int j=0;j<data[0].length;j++)
				out[j][i]=data[i][j];
		return new Matrix(out);
	}
}




