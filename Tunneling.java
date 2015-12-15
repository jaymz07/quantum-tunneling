/* Java Program for generating analog X,Y signals for a laser projector using the analog soundcard output.
 * Drawing on graphical window reflects the laser output.
 * This class defines a window, gets some points from mouse input and passes these points to a seperate
 * thread defined by LaserOutputThread.java, which handles all audio output.
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Tunneling implements MouseListener, MouseMotionListener, KeyListener {

    JFrame frame;
    DrawPanel drawPanel;


    Graphics page;

    int width = 500, height = 500;

    double xLower = -30.0, xUpper = 20.0, xInc = 0.05;

    double xcInitial = -10.0;

    double barrierWidth = 1.0, barrierHeight = 0.01;
    double barrierGraphicalHeight = 5.0;

    double time = 0.0, timeStep = 0.000001;

    int x0Index=-1, xaIndex=-1;

    Complex [] wavefunction;
    double [] xArray;

    Complex II = new Complex(0,1);

    DataSet vGraph;

    public void run() {




        /*---------Make window-----------*/
        frame = new JFrame("Tunneling Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //window options
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setSize(width+20,height+40);

        //drawPanel actually writes to the screen. frame is just a container
        drawPanel = new DrawPanel();
        frame.getContentPane().add(BorderLayout.CENTER, drawPanel);

        //Allow keboard and mouse input for the window
        //also tells frame to call method overrides for the listeners
        frame.addMouseListener(this);
        frame.addMouseMotionListener( this );
        frame.addKeyListener ( this ) ;

        wavefunction = makeWavepacket();
        xArray = makeDomain();

        vGraph = new DataSet(xArray,potentialFunction(xArray));

        while( true ) {
            frame.repaint();
            wavefunction = evolveTimeStep(wavefunction,xArray,timeStep);
            time+= timeStep;
        }
    }

    /*--------Nested class defining object to be painted to screen.-------
    	  Simply overrides paintComponent() originally defined in JPanel*/
    class DrawPanel extends JPanel {

        //Overrides method defined in JPanel superclass. Called when repaint() is called from frame.
        public void paintComponent(Graphics g) {
            page = g;
            //page = iOut.getGraphics();

            page.setColor(Color.BLACK);
            page.fillRect(0,0,width,height);

            page.setColor(Color.GREEN);

            if(wavefunction != null) {
                ArrayList<DataSet> graphList = new ArrayList<DataSet>();
                graphList.add(generateGraphAbs(wavefunction,xArray));
              //  graphList.add(generateGraphRe(wavefunction,xArray));
              //  graphList.add(generateGraphIm(wavefunction,xArray));
                graphList.add(vGraph);
                multiGraph graph = new multiGraph(page, graphList, width, height);
                graph.printGraph();
            }

        }
    }
    
//------Initial Data---------------
    
    public Complex [] makeWavepacket()
    {
        ArrayList<Complex> out = new ArrayList<Complex>();
        int count =0;
        double integralConj = 0;
        for (double x = xLower; x <= xUpper; x += xInc)
        {
            Complex sum = new Complex(0,0);
            for(double k = 0; k< 30 ; k+=0.001) {
                sum = sum.plus(sumTerm(x,k));
            }
            if(Math.abs(x) < xInc/2)
                x0Index = count;
            if(Math.abs(x - barrierWidth) < xInc/2)
                xaIndex = count;
            out.add(sum);
            integralConj += sum.absSqr();
            count++;
        }
        integralConj *= ((double)(xUpper-xLower))/count;
        for(int i =0; i< count; i++) {
            out.set(i,out.get(i).times(1.0/Math.sqrt(integralConj)));
        }
        Complex [] outArr = out.toArray(new Complex[count]);
        return outArr;
    }
    public Complex sumTerm(double x, double k)
    {
        //if (x < 0.0)
        return new Complex(0, (x-xcInitial) * k).exp().times(aCoeff(k));


    }

    public double aCoeff(double k)
    {
        return Math.exp(-k * k / 10000);
    }
    public double [] potentialFunction(double [] x) {
        double [] out = new double[x.length];
        for(int i =0; i<x.length; i++) {
            if(x[i]>0 && x[i] < barrierWidth)
                out[i]=barrierGraphicalHeight;
            else
                out[i] = 0.0;
        }
        return out;
    }
    public double [] makeDomain()
    {
        ArrayList<Double> out = new ArrayList<Double>();
        int count =0;
        for (double x = xLower; x <= xUpper; x += xInc)
        {
            out.add(x);
            count++;
        }
        double [] primitives = new double[out.size()];
        for(int i =0; i<out.size(); i++)
            primitives[i]=out.get(i);
        return primitives;
    }

//---------Time evolution-----------

    public Complex [] evolveTimeStep(Complex [] psi, double [] x, double tStep) {
        int n = psi.length;
        Complex [] k1 = computeK(psi,x,0.0);
	
        //linear step:
        //return addArraysWithFactor(psi,k1,1.0*tStep);
	//Runge-Kutta 3rd Order:
        Complex [] k2 = computeK( addArraysWithFactor(psi, k1, 0.5*tStep), x, tStep/2 );
        Complex [] k3 = computeK( addArraysWithFactor(psi, k2, 0.5*tStep), x, tStep/2 );
        Complex [][] sum = {psi, arrayTimesFactor(k1,1.0/6*tStep), arrayTimesFactor(k2,2.0/3*tStep), arrayTimesFactor(k3,1.0/6*tStep) };
        return sumArrays(sum);
        
    }


    public Complex [] computeK(Complex [] psi, double [] x, double tStep) {

        int n = psi.length;
        Complex [] out = new Complex[n];
        out[0]   = new Complex(0,0);
        out[n-1] = new Complex(0,0);
        for(int i =1; i< n-1; i++) {
            double potential = 0.0;
            if(i == x0Index || i == xaIndex)
                potential = barrierHeight/2;
            else if(i > x0Index && i < xaIndex)
                potential = barrierHeight;
            Complex laplacian = (psi[i+1].minus(psi[i].times(2.0)).plus(psi[i-1])).times(1.0/xInc/xInc);
            Complex partialT = II.times(0.5).times(laplacian).minus(II.times(potential));
            out[i] = partialT;
        }
        return out;
    }

//----------Array Helper methods------------------------

    public Complex [] addArraysWithFactor(Complex [] a, Complex [] b, double factor) {
        int n = a.length;
        Complex [] out = new Complex[n];
        for(int i =0; i<n; i++)
            out[i] = a[i].plus(b[i].times(factor));
        return out;
    }
    public Complex [] arrayTimesFactor(Complex [] arr, double factor) {
        Complex [] product = new Complex[arr.length];
        for(int i = 0; i< arr.length; i++)
            product[i] = arr[i].times(factor);
        return product;
    }
    public Complex [] sumArrays(Complex [][] arr) {
        Complex [] sum = new Complex[arr[0].length];
        for(int i = 0; i<arr[0].length; i++) {
	  Complex sumI = new Complex(0,0);
	  for(int j=0;j<arr.length; j++)
            sumI= sumI.plus(arr[j][i]);
	  sum[i] = sumI;
	}
        return sum;
    }

//---------Generate Display points------------

    public DataSet generateGraphAbs(Complex [] psi, double [] x)
    {
        ArrayList<Point> out = new ArrayList<Point>();
        int n = psi.length;
        for(int i =0; i< n; i++) {
            out.add(new Point(x[i], psi[i].absSqr()));
        }
        return new DataSet(out);
    }
    public DataSet generateGraphIm(Complex [] psi, double [] x)
    {
        ArrayList<Point> out = new ArrayList<Point>();
        int n = psi.length;
        for(int i =0; i< n; i++) {
            out.add(new Point(x[i], psi[i].im));
        }
        return new DataSet(out);
    }
    public DataSet generateGraphRe(Complex [] psi, double [] x)
    {
        ArrayList<Point> out = new ArrayList<Point>();
        int n = psi.length;
        for(int i =0; i< n; i++) {
            out.add(new Point(x[i], psi[i].re));
        }
        return new DataSet(out);
    }


    /*----------Main function. Calls constructor of whole class-----------*/
    public static void main(String[] args) {
        for(String s : args)
            System.out.println(s);
        (new Tunneling()).run() ;
    }

    /*----------Keyboard Input. Event generated function calls.----------------------*/

    public void keyTyped ( KeyEvent e ) { }
    public void keyPressed ( KeyEvent e) {
        //System.out.println("KEY");
        if(e.getKeyCode()== KeyEvent.VK_C) {
        }

        frame.repaint();
    }
    public void keyReleased ( KeyEvent e ) {}

    /*-----------Mouse Input. Event generated function calls------------------------*/
    public void mouseEntered( MouseEvent e ) {
        // called when the pointer enters the applet's rectangular area
    }
    public void mouseExited( MouseEvent e ) {}  // called when the pointer leaves the applet's rectangular area
    public void mouseClicked( MouseEvent e ) {}
    public void mousePressed( MouseEvent e ) {  // called after a button is pressed down
        int xP=e.getX();
        int yP=e.getY();
        frame.repaint();
    }
    public void mouseReleased( MouseEvent e ) {}  // called after a button is released
    public void mouseMoved( MouseEvent e )    {}  // called during motion when no buttons are down
    public void mouseDragged( MouseEvent e )  {   // called during motion with buttons down
        int xP=e.getX();
        int yP=e.getY();

        frame.repaint();
    }
}



