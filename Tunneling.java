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
    boolean showImag = false, showReal = false;
    
    //X axis settings
    double xLower = -7.0, xUpper = 5.0, dX_Base = 0.04;
    double xcInitial = -2.0;
    
    //Pulse Settings
    double gaussWidth = 0.6, pulseMomentum = 100;

    //Barrier Settings
    double barrierWidth_base = 0.75;
    double barrierGraphicalHeight = 0.09;
    
    //Physics constants
    double mass = 100, hbar = 1;
    
    //Integration Settings
    double time = 0.0, timeStepBase = 0.0002;
    IntegrationMode stepMode = IntegrationMode.RICHARDSON;
    
  //----------------------Some global constants-----------------------
    public enum IntegrationMode{	
      FEULER,RK3,RICHARDSON
    }
    Complex [] wavefunction;
    Complex [] EtoV;
    Complex II = new Complex(0,1);
    Complex alpha, beta;
    double [] xArray;
    double epsilon, timeStep,  xInc = dX_Base/6;
    int x0Index = -1, xaIndex = -1;
    boolean reset = false;
    double barrierHeight, barrierWidth, initFreq;
    
    
    JFrame frame;
    DrawPanel drawPanel;
    Graphics page;
    DataSet vGraph;
    
    ControlPanel controlPanel;

    public void run() {

        /*---------Make window-----------*/
        frame = new JFrame("Tunneling Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
	/*--------Make Control Panel Window-------------*/
	controlPanel = new ControlPanel("Control Panel");
	controlPanel.pack();
	controlPanel.setVisible(true);
	controlPanel.setResizable(false);
	controlPanel.setLocation(500,0);

        //window options
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setSize(width,height);

        //drawPanel actually writes to the screen. frame is just a container
        drawPanel = new DrawPanel();
        frame.getContentPane().add(BorderLayout.CENTER, drawPanel);

        //Allow keboard and mouse input for the window
        //also tells frame to call method overrides for the listeners
        frame.addMouseListener(this);
        frame.addMouseMotionListener( this );
        frame.addKeyListener ( this ) ;

	timeStep=timeStepBase/5;
	barrierWidth  = barrierWidth_base*0.5;
	initFreq = pulseMomentum*50/100;
	barrierHeight = initFreq*initFreq/2/mass*50.0/100*2;
	
        makeWavepacket();
	
	//System.out.println("Wavfunction Energy = " + computeWavefunctionEnergy(wavefunction));

        vGraph = new DataSet(xArray,potentialFunctionGraphical(xArray));

        while( true ) {
            frame.repaint();
	    if(!reset) {
	      switch(stepMode) {
		case FEULER:
		  wavefunction = linearStep(wavefunction,xArray,timeStep);
		case RK3:
		  wavefunction = evolveTimeStepRK3(wavefunction,xArray,timeStep);
		  break;
		case RICHARDSON:
		  wavefunction = step(wavefunction);
		  break;
	      }
	      
	      time+= timeStep;
	    }
	    else {
	      makeWavepacket();
	      time = 0;
	      reset = false;
	    }
        }
    }

    /*----------------Nested class defining object to be painted to screen.--------------------
    	  Simply overrides paintComponent() defined in JPanel*/
    class DrawPanel extends JPanel {

        //Overrides method defined in JPanel superclass. Called when repaint() is called from frame.
        public void paintComponent(Graphics g) {
            page = g;
            //page = iOut.getGraphics();

            page.setColor(Color.WHITE);
            page.fillRect(0,0,width,height);

            page.setColor(Color.GREEN);
	    
	    //Generate graphs of all Data Sets
            if(wavefunction != null) {
                
		ArrayList<DataSet> graphList = new ArrayList<DataSet>();
		
		//Set y axis bounds by using phony graph
		ArrayList<Point> yAxisSet = new ArrayList<Point>();
		yAxisSet.add(new Point(0,barrierGraphicalHeight*1.25));
		if(showImag || showReal)
		  yAxisSet.add(new Point(0,-barrierGraphicalHeight*1.25));
		else
		  yAxisSet.add(new Point(0,-barrierGraphicalHeight/3));
		graphList.add(new DataSet(yAxisSet));
		
		//graph of psi squared
		DataSet psiSquaredGraph = generateGraphAbs(wavefunction,xArray);
		//psiSquaredGraph.pSize = 2;
		graphList.add(psiSquaredGraph);
		
		if(showReal)
		  graphList.add(generateGraphRe(wavefunction,xArray));
		if(showImag)
		  graphList.add(generateGraphIm(wavefunction,xArray));
		
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
	    //Normalize wavefunction:
            psi.set(i,psi.get(i).times(1.0/Math.sqrt(integralConj)));
	    //For time independent potentials:
	    double r = potentialFunction(xArray[i],0.0) * timeStep / hbar;
	    EtoV[i] = new Complex(0,-r).exp();
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
    public double potentialFunction(double x, double t) {
        return (x>0 && x < barrierWidth) ? barrierHeight : 0;
    }
    
    public Complex computeWavefunctionEnergy(Complex [] psi) {
      int n=wavefunction.length;
      Complex sum = new Complex(0,0);
      for(int i =1;i<n-1;i++) {
	Complex laplacian = (psi[i+1].minus(psi[i].times(2.0)).plus(psi[i-1])).times(1.0/xInc/xInc);
	sum.add(laplacian,sum);
      }
      return sum.times( xInc );
    }
    
//---------Time evolution-----------

    public Complex [] evolveTimeStepRK3(Complex [] psi, double [] x, double tStep) {
        int n = psi.length;
        Complex [] k1 = computeK(psi,x,0.0);
	
        //Runge-Kutta 3rd Order:
        Complex [] k2 = computeK( addArraysWithFactor(psi, k1, 0.5*tStep), x, tStep/2 );
        Complex [] k3 = computeK( addArraysWithFactor(psi, k2, 0.5*tStep), x, tStep/2 );
        Complex [][] sum = {psi, arrayTimesFactor(k1,1.0/6*tStep), arrayTimesFactor(k2,2.0/3*tStep), arrayTimesFactor(k3,1.0/6*tStep) };
        return sumArrays(sum);
    }
    public Complex [] linearStep(Complex [] psi, double [] x, double tStep) {
        int n = psi.length;
        Complex [] k1 = computeK(psi,x,0.0);

        return addArraysWithFactor(psi,k1,1.0*tStep);
    }
    public Complex [] computeK(Complex [] psi, double [] x, double tStep) {

        int n = psi.length;
        Complex [] out = new Complex[n];
        out[0]   = new Complex(0,0);
        out[n-1] = new Complex(0,0);
        for(int i =1; i< n-1; i++) {
            double potential = potentialFunction(x[i],time+tStep);
            Complex laplacian = (psi[i+1].minus(psi[i].times(2.0)).plus(psi[i-1])).times(1.0/xInc/xInc);
            Complex partialT = II.times(0.5*hbar/mass).times(laplacian.times(1.0/hbar)).plus(psi[i].times(-potential));
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
            out.add(new Point(x[i], psi[i].im*psi[i].im*Math.signum(psi[i].im)));
        }
        return new DataSet(out);
    }
    public DataSet generateGraphRe(Complex [] psi, double [] x)
    {
        ArrayList<Point> out = new ArrayList<Point>();
        int n = psi.length;
        for(int i =0; i< n; i++) {
            out.add(new Point(x[i], psi[i].re*psi[i].re*Math.signum(psi[i].re)));
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
    
    //Add labels to JSlider class
    public class LabelSlider extends JSlider {
        public String label;
        public LabelSlider(String label, int orientation, int min, int max, int value) {
            super(orientation, min, max, value);
            super.setPaintLabels(true);
            this.label = label;
        }
    }
    
    //-------------------------------------Defines Control Window in a nested class------------------------------------------------------
    
    public class ControlPanel extends Frame implements WindowListener, ActionListener, ChangeListener, ItemListener {
        public ControlPanel(String title) {
            super(title);
            setLayout(new FlowLayout());
            addWindowListener(this);

            //-------make buttons-----
            ArrayList<Button> buttons = new ArrayList<Button>();
	    
            buttons.add(new Button("Reset"));
	    
            JPanel buttonPanel = new JPanel(new GridLayout(0,1));
            for(Button b : buttons) {
                b.addActionListener(this);
                buttonPanel.add(b);
            }
            add(buttonPanel);

            //---------Make View checkboxes--------------
            ArrayList<JCheckBox> vcBoxes = new ArrayList<JCheckBox>();
	    JPanel vcPanel = new JPanel(new GridLayout(0,1));
	    vcPanel.add(new JLabel("View Controls"));
            vcBoxes.add(new JCheckBox("Real Part"));
            vcBoxes.get(vcBoxes.size()-1).setName("Real Part");
	    vcBoxes.add(new JCheckBox("Imag Part"));
            vcBoxes.get(vcBoxes.size()-1).setName("Imag Part");
            for(JCheckBox b : vcBoxes) {
                b.addItemListener(this);
                vcPanel.add(b);
            }
            add(vcPanel);

            //----------Make sliders---------------
            ArrayList<LabelSlider> sliders = new ArrayList<LabelSlider>();
	    
            sliders.add(	new LabelSlider("Timestep",JSlider.HORIZONTAL,1,10,5));
	    sliders.add(	new LabelSlider("dX",JSlider.HORIZONTAL,1,10,5));
	    LabelSlider b_en  = new LabelSlider("Barrier Energy",JSlider.HORIZONTAL,0,100,50);
	    
	    //Set labels for barrier energy
	    Hashtable labeltable = new Hashtable();
	    labeltable.put(new Integer(0), new JLabel("0"));
	    labeltable.put(new Integer(50), new JLabel("E"));
	    labeltable.put(new Integer(100), new JLabel("2E"));
	    b_en.setLabelTable(labeltable);
	    b_en.setPaintLabels(true);
	    sliders.add(b_en);
	    
	    sliders.add(	new LabelSlider("Barrier Width",JSlider.HORIZONTAL,1,100,50));
	    sliders.add(	new LabelSlider("Pulse Momentum",JSlider.HORIZONTAL,10,100,30));
	    
            JPanel sliderPanel = new JPanel(new GridLayout(0,1));
            for(LabelSlider s : sliders) {
		JLabel label = new JLabel(s.label,JLabel.CENTER);
                label.setAlignmentX(s.getAlignmentX());
                label.setAlignmentY(Component.BOTTOM_ALIGNMENT);
                sliderPanel.add(label);
                
                sliderPanel.add(s);
		s.addChangeListener(this);
            }
            add(sliderPanel);
        }

        public void windowClosing(WindowEvent e) {
            dispose();
            System.exit(0);
        }
        //Neccesary overides to implement appropriate listeners
        public void windowOpened(WindowEvent e) {}
        public void windowActivated(WindowEvent e) {}
        public void windowIconified(WindowEvent e) {}
        public void windowDeiconified(WindowEvent e) {}
        public void windowDeactivated(WindowEvent e) {}
        public void windowClosed(WindowEvent e) {}

        //Button events
        public void actionPerformed(ActionEvent e) {
            String actionString = e.getActionCommand();
            System.out.println(actionString);
            if(actionString == "Reset")
                reset=true;
            frame.repaint();
        }

        //Sider Events
        public void stateChanged(ChangeEvent e) {
            LabelSlider source = (LabelSlider)e.getSource();
            if (!source.getValueIsAdjusting()) {
                if(source.label.equals("Timestep")) {
                    timeStep = timeStepBase/(11 - (int)source.getValue());
		    reset = true;
		}
		if(source.label.equals("dX")) {
                    xInc = dX_Base/(11 - (int)source.getValue());
		    reset = true;
		}
		if(source.label.equals("Barrier Energy")) {
                    barrierHeight = initFreq*initFreq/2/mass*((int)source.getValue())/100*2;
		    reset = true;
		}
            }
            else {
	      if(source.label.equals("Barrier Width")) {
                    barrierWidth = barrierWidth_base*((int)source.getValue())/100;
		    vGraph = new DataSet(xArray,potentialFunctionGraphical(xArray));
		    reset = true;
		}
	      if(source.label.equals("Pulse Momentum")) {
		    barrierHeight /= initFreq*initFreq;
                    initFreq = pulseMomentum*((int)source.getValue())/100;
		    barrierHeight *= initFreq*initFreq;
		    reset = true;
		}
	    }
            System.out.println(source.label + " = " + (int)source.getValue());
            frame.repaint();
        }

        //Check box events
        public void itemStateChanged(ItemEvent e) {
            JCheckBox source = (JCheckBox)e.getItemSelectable();
            if(source.getName().equals("Real Part")) {
                if(e.getStateChange() == 2)
                    showReal = false;
                else
                    showReal = true;
            }
            if(source.getName().equals("Imag Part")) {
                if(e.getStateChange() == 2)
                    showImag = false;
                else
                    showImag = true;
            }
            frame.repaint();
        }
        
    }
}






