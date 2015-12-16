import java.io.*;
import java.util.*;
import java.math.*;

public class Complex
{
	public double re;
	public double im;

	public Complex()
	{
		re = 0.0;
		im = 0.0;
	}

	public Complex(double r, double i)
	{
		re=r;
		im=i;
	}
	//---------Inline Math functions---------------------
	public Complex plus(Complex b)
	{
		return new Complex(re+b.re,im+b.im);
	}
	public Complex plus(double b)
	{
		return new Complex(re+b,im);
	}
	public Complex minus(Complex b)
	{
	      return new Complex(re - b.re, im - b.im);
	}
	public Complex minux(double b)
	{
		return new Complex(re-b,im);
	}
	public Complex times(double b)
	{
		return new Complex(re*b,im*b);
	}
	public Complex times(Complex b)
	{
		return new Complex(re*b.re-im*b.im,re*b.im+im*b.re);
	}
	public Complex dividedBy(Complex b)
	{
		double denom=b.absSqr();
		return (new Complex(b.re/denom,-b.im/denom)).times(this);
	}
	public String toString()
	{
		return re+"+"+im+"*i";
	}
	public double abs(){ return Math.sqrt(re*re+im*im); }
	public double absSqr() { return re*re + im*im; }
	public double getAngle()
	{
		return Math.atan2(im,re);
	}
	public Complex pow(double b)
	{
		double mag=Math.pow(abs(),b),ang=getAngle();
		return new Complex(mag*Math.cos(ang*b),mag*Math.sin(ang*b));
	}
	public Complex exp()
	{
		return (new Complex(Math.cos(im),Math.sin(im))).times(Math.exp(re));
	}
	public Complex sin()
	{
		return ((new Complex(0,1)).times(this)).exp().plus((((new Complex(0,-1)).times(this)).times(-1)).exp()).dividedBy(new Complex(0,2));
	}
	public Complex cos()
	{
		return ((new Complex(0,1)).times(this)).exp().plus(((new Complex(0,-1)).times(this)).exp()).times(.5);
	}
	
	//-------------Modifier Math functions----------------
	
	public void set(Complex a) {
		re=a.re;
		im=a.im;
	}
	public void add(Complex a, Complex b) {
		re = a.re + b.re;
		im = a.im + b.im;
	}
	public void mult(Complex a, Complex b) {
		re = a.re*b.re - a.im*b.im;
		im = a.re*b.im + a.im*b.re;
	}
}




