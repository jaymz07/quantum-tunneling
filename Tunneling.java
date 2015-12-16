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
    
  //-------------------Main Program Parameters-----------------------
    //Window settings
    int width = 500, height = 500;
    
    //X axis settings
    double xLower = -10.0, xUpper = 10.0, xInc = 0.05;
    double xcInitial = -2.0;

    //Barrier Settings
    double barrierWidth = 0.2, barrierHeight = 5;
    double barrierGraphicalHeight = 0.05;
    
    //Timestep Settings
    double time = 0.0, timeStep = 0.00002;
    double gaussWidth = 0.5, initFreq = 30;
    
    //Physics constants
    double mass = 100, hbar = 1;
    
    //------------------Some global constants-----------
    Complex [] wavefunction, EtoV;
    Complex II = new Complex(0,1);
    Complex alpha, beta;
    double [] xArray;
    double epsilon;
    int x0Index = -1, xaIndex = -1;
    
    JFrame frame;
    DrawPanel drawPanel;
    Graphics page;
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

        makeWavepacket();

        vGraph = new DataSet(xArray,potentialFunctionGraphical(xArray));

        while( true ) {
            frame.repaint();
            //wavefunction = evolveTimeStepRK3(wavefunction,xArray,timeStep);
	    wavefunction = step(wavefunction);
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

            page.setColor(Color.WHITE);
            page.fillRect(0,0,width,height);

            page.setColor(Color.GREEN);
            ArrayList<Point> yAxisSet = new ArrayList<Point>();
            yAxisSet.add(new Point(0,-barrierGraphicalHeight/3));
            yAxisSet.add(new Point(0,barrierGraphicalHeight*1.25));
            if(wavefunction != null) {
                ArrayList<DataSet> graphList = new ArrayList<DataSet>();
                //graphList.add(generateGraphRe(wavefunction,xArray));
                //graphList.add(generateGraphIm(wavefunction,xArray));
                graphList.add(generateGraphAbs(wavefunction,xArray));
                graphList.add(new DataSet(yAxisSet));
                graphList.add(vGraph);
                multiGraph graph = new multiGraph(page, graphList, width, height);
                graph.printGraph();
            }

        }
    }

//------Initial Data---------------

    public void makeWavepacket()
    {
        ArrayList<Complex> psi = new ArrayList<Complex>();
        int count =0;
        double integralConj = 0;
        for (double x = xLower; x <= xUpper; x += xInc)
        {
            if(Math.abs(x) < xInc/2)
                x0Index = count;
            if(Math.abs(x - barrierWidth) < xInc/2)
                xaIndex = count;
	    Complex oscFunc = (new Complex(0,x*initFreq)).exp();
            psi.add(oscFunc.times(envelopeFunction(x)));
            integralConj += oscFunc.absSqr();
            count++;
        }
        integralConj *= ((double)(xUpper-xLower))/count;
	EtoV = new Complex[count];
	xArray = new double[count];
	//timeStep = 0.8 * xInc * xInc / hbar;
	epsilon = hbar*timeStep/(mass * xInc * xInc);
	alpha = new Complex(0.5 * (1.0+Math.cos(epsilon/2)) , -0.5 * Math.sin(epsilon/2));
	beta  = new Complex((Math.sin(epsilon/4))*Math.sin(epsilon/4), 0.5 * Math.sin(epsilon/2));
        for(int i =0; i< count; i++) {
	    xArray[i] = xLower + xInc*i;
            psi.set(i,psi.get(i).times(1.0/Math.sqrt(integralConj)));
	    double r = potentialFunction(xArray[i]) * timeStep /hbar;
	    EtoV[i] = new Complex(0,-r).exp();
            //out.set(i,new Complex(0,0));
        }
        wavefunction = psi.toArray(new Complex[count]);
    }
    
    public double envelopeFunction(double x) {
        return Math.exp(-(x-xcInitial)*(x-xcInitial)/gaussWidth/gaussWidth);
    }
    public double [] potentialFunctionGraphical(double [] x) {
        double [] out = new double[x.length];
        for(int i =0; i<x.length; i++) {
            if(x[i]>0 && x[i] < barrierWidth)
                out[i]=barrierGraphicalHeight;
            else
                out[i] = 0.0;
        }
        return out;
    }
    public double potentialFunction(double x) {
        return (x>0 && x < barrierWidth) ? barrierHeight : 0;
    }
    
//---------Time evolution-----------

    public Complex [] evolveTimeStepRK3(Complex [] psi, double [] x, double tStep) {
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
            Complex partialT = II.times(0.5*hbar/mass).times(laplacian.times(1.0/hbar)).plus(-potential);
            out[i] = partialT;
        }
        return out;
    }
    
//----------Richardson algorithm----------------
    
    public Complex [] step(Complex [] psi) {
        Complex x = new Complex(0,0);
        Complex y = new Complex(0,0);
        Complex w = new Complex(0,0);
        Complex z = new Complex(0,0);
	
	int nx = psi.length;
        /*
         * The time stepping algorithm used here is described in:
         *
         * Richardson, John L.,
         * Visualizing quantum scattering on the CM-2 supercomputer,
         * Computer Physics Communications 63 (1991) pp 84-94
         */

        for(int i=0; i<nx-1; i+=2) {
            x.set(psi[i]);
            y.set(psi[i+1]);
            w.mult(alpha,x);
            z.mult(beta,y);
            psi[i+0].add(w,z);
            w.mult(alpha,y);
            z.mult(beta,x);
            psi[i+1].add(w,z);
        }

        for(int i=1; i<nx-1; i+=2) {
            x.set(psi[i]);
            y.set(psi[i+1]);
            w.mult(alpha,x);
            z.mult(beta,y);
            psi[i+0].add(w,z);
            w.mult(alpha,y);
            z.mult(beta,x);
            psi[i+1].add(w,z);
        }

        x.set(psi[nx-1]);
        y.set(psi[0]);
        w.mult(alpha,x);
        z.mult(beta,y);
        psi[nx-1].add(w,z);
        w.mult(alpha,y);
        z.mult(beta,x);
        psi[0   ].add(w,z);

        for(int i=0; i<nx; i++) {
            x.set(psi[i]);
            psi[i].mult(x,EtoV[i]);
        }

        x.set(psi[nx-1]);
        y.set(psi[0]);
        w.mult(alpha,x);
        z.mult(beta,y);
        psi[nx-1].add(w,z);
        w.mult(alpha,y);
        z.mult(beta,x);
        psi[0   ].add(w,z);

        for(int i=1; i<nx-1; i+=2) {
            x.set(psi[i]);
            y.set(psi[i+1]);
            w.mult(alpha,x);
            z.mult(beta,y);
            psi[i+0].add(w,z);
            w.mult(alpha,y);
            z.mult(beta,x);
            psi[i+1].add(w,z);
        }

        for(int i=0; i<nx-1; i+=2) {
            x.set(psi[i]);
            y.set(psi[i+1]);
            w.mult(alpha,x);
            z.mult(beta,y);
            psi[i+0].add(w,z);
            w.mult(alpha,y);
            z.mult(beta,x);
            psi[i+1].add(w,z);
        }
        return psi;
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
            for(int j=0; j<arr.length; j++)
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



