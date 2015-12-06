/*
 * Copyright (C) 2015 Redjan Shabani
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package seatscalculator;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Redjan Shabani
 */
public class JFrameMain extends javax.swing.JFrame
{
	private static final int HARE_NEIMEYER = 1;
	private static final int DHONDT = 2;
	private static final int SAINTE_LAGUE = 3;
	
	private int seats = 0;//number of seats
	private float coalThresh = 0.0f, partyThresh = 0.0f, indivThresh = 0.0f;//thresholds
	private int method = DHONDT;
	
	private List<Subject> subjects = new ArrayList();
	
	public JFrameMain()
	{
		initComponents();
		
		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				exitOp();
			}
		});
	}
	
	private void exitOp()
	{
		int i = JOptionPane.showConfirmDialog(this, "Exit the program?", "Confirm!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
          if(i==0)
			System.exit(0);//cierra aplicacion
	}
	
	private void addSubject(Subject subject)
	{
		this.subjects.add(subject);
		this.calculateVotingResults();
	}
	
	private void calculateVotingResults()
	{
		//extracting voting array
		int[] votes = new int[this.subjects.size()];
		for(int i=0; i<this.subjects.size(); i++)
			votes[i] = this.subjects.get(i).getVotes();
		
		Calculator calculator = new Calculator(
			   this.seats, 
			   votes
		);
		
		float[] prc = calculator.getPercentages();
		float[] ids = calculator.getIdealSeats();
		int[] res = calculator.getSeats(this.method);
		for(int i=0; i<this.subjects.size(); i++)
		{
			this.subjects.get(i).setPercentage(prc[i]);
			this.subjects.get(i).setIdealSeats(ids[i]);
			this.subjects.get(i).setRealSeats(res[i]);
		}
		
		//calculating seats by considerin thresholds
		int[] votesTh = new int[this.subjects.size()];
		for(int i=0; i<this.subjects.size(); i++)
		{
			if(this.subjects.get(i).getCategory() == Subject.COALITION)
			{
				if(prc[i]<this.coalThresh)
					votesTh[i] = 0;
				else
					votesTh[i] = votes[i];
			}
			if(this.subjects.get(i).getCategory() == Subject.PARTY)
			{
				if(prc[i]<this.partyThresh)
					votesTh[i] = 0;
				else
					votesTh[i] = votes[i];
			}
			if(this.subjects.get(i).getCategory() == Subject.INDIVIDUAL)
			{
				if(prc[i]<this.indivThresh)
					votesTh[i] = 0;
				else
					votesTh[i] = votes[i];
			}
		}
		calculator = new Calculator(
			   this.seats, 
			   votesTh
		);
		
		res = calculator.getSeats(this.method);
		for(int i=0; i<this.subjects.size(); i++)
			this.subjects.get(i).setRealSeats(res[i]);
		
		this.updateTable();
	}
	
	private void updateTable()
	{
		String[] header = {"Category", "Subject", "Votes", "%", "Portion", "Seats", "Error"};
		Object[][] data = new Object[this.subjects.size()+1][7];
		
		int totVotes = 0;
		float totProp = 0;
		int totSeats = 0;
		float totPerc = 0;
		float totErr = 0;
		for(int i=0; i<this.subjects.size(); i++)
		{
			if(this.subjects.get(i).getCategory()==Subject.COALITION)
				data[i][0] = "Coalition";
			else if(this.subjects.get(i).getCategory()==Subject.PARTY)
				data[i][0] = "Party";
			else if(this.subjects.get(i).getCategory()==Subject.INDIVIDUAL)
				data[i][0] = "Individual";
			
			data[i][1] = "<html><h2>" + this.subjects.get(i).getName() + "</h2><html>";
			data[i][2] = this.subjects.get(i).getVotes();
			data[i][3] = Math.round(100 * this.subjects.get(i).getPercentage()) / 100.0f;
			data[i][4] = Math.round(100 * this.subjects.get(i).getIdealSeats()) / 100.0f;
			data[i][5] = "<html><h2>" + this.subjects.get(i).getRealSeats() + "</h2><html>";
			data[i][6] = Math.round(100.0f * Math.abs(this.subjects.get(i).getIdealSeats() - (float)this.subjects.get(i).getRealSeats())) / 100.0f;
			
			totVotes += this.subjects.get(i).getVotes();
			totProp += this.subjects.get(i).getIdealSeats();
			totPerc += this.subjects.get(i).getPercentage();
			totSeats += this.subjects.get(i).getRealSeats();
			totErr += Math.abs(this.subjects.get(i).getIdealSeats() - (float)this.subjects.get(i).getRealSeats());
		}
		
		data[this.subjects.size()][1] = "<html><h2>Total</h2><html>";
		data[this.subjects.size()][2] = totVotes;
		data[this.subjects.size()][3] = Math.round(100 * totPerc) / 100.0f;
		data[this.subjects.size()][4] = Math.round(100 * totProp) / 100.0f;
		data[this.subjects.size()][5] = "<html><h2>" + totSeats + "</h2><html>";
		data[this.subjects.size()][6] = Math.round(100.0f * totErr) / 100.0f;
		
		DefaultTableModel dtm = new DefaultTableModel(data, header)
		{
			@Override
			public boolean isCellEditable(int r, int c)
			{
				return false;
			}	
		};
		
		this.jTable1.setModel(dtm);
		
		DefaultTableCellRenderer ldtcr = new DefaultTableCellRenderer();
		ldtcr.setHorizontalAlignment(SwingConstants.LEFT);
		this.jTable1.getColumn("Category").setCellRenderer(ldtcr);
		this.jTable1.getColumn("Subject").setCellRenderer(ldtcr);
		
		DefaultTableCellRenderer rdtcr = new DefaultTableCellRenderer();
		rdtcr.setHorizontalAlignment(SwingConstants.RIGHT);
		this.jTable1.getColumn("Votes").setCellRenderer(rdtcr);
		this.jTable1.getColumn("%").setCellRenderer(rdtcr);
		this.jTable1.getColumn("Portion").setCellRenderer(rdtcr);
		this.jTable1.getColumn("Seats").setCellRenderer(rdtcr);
		this.jTable1.getColumn("Error").setCellRenderer(rdtcr);
		
		DefaultTableCellRenderer cdtcr =  new DefaultTableCellRenderer();
		cdtcr.setHorizontalAlignment(SwingConstants.CENTER);
		cdtcr.setBackground(Color.GRAY);
		cdtcr.setForeground(Color.WHITE);
		
		this.jTable1.getColumn("Category").setPreferredWidth(200);
		this.jTable1.getColumn("Subject").setPreferredWidth(600);
		this.jTable1.setShowHorizontalLines(true);
	}
	
	private void loadFromXML(String fileName)
	{
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new File(fileName));
			
			NodeList nodeList = doc.getElementsByTagName("calculator");
			if(nodeList.getLength()==0)
				return;
			Element element = (Element) nodeList.item(0);
			this.seats = Integer.parseInt(element.getAttribute("seats")); this.jSpinnerTotSeats.setValue(this.seats);
			this.coalThresh = Float.parseFloat(element.getAttribute("coalTh")); this.jSpinnerCoalThresh.setValue(this.coalThresh);
			this.partyThresh = Float.parseFloat(element.getAttribute("partyTh")); this.jSpinnerPartyThresh.setValue(this.partyThresh);
			this.indivThresh = Float.parseFloat(element.getAttribute("indivTh")); this.jSpinnerIndivThresh.setValue(this.indivThresh);
			switch (element.getAttribute("method"))
			{
				case "DHONDT":
					this.method = Calculator.DHONDT_METHOD;
					break;
				case "SAINT-LAGUE":
					this.method = Calculator.SAINT_LAGUE_METHOD;
					break;
				case "HARE-NIEMEYER":
					this.method = Calculator.HARE_NIEMEYER_METHOD;
					break;
			}
			
			this.subjects = new ArrayList();
			nodeList = doc.getElementsByTagName("subject");
			for(int i=0; i<nodeList.getLength(); i++)
			{
				element = (Element) nodeList.item(i);
				
				int cat = Subject.PARTY;
				switch (element.getAttribute("category"))
				{
					case "COALITION":
						cat = Subject.COALITION;
						break;
					case "PARTY":
						cat = Subject.PARTY;
						break;
					case "INDIVIDUAL":
						cat = Subject.INDIVIDUAL;
						break;
				}
				
				String name = element.getAttribute("name");
				int votes = Integer.parseInt(element.getAttribute("votes"));
				
				this.subjects.add(new Subject(cat, name, votes));
			}
			
			this.calculateVotingResults();
		}
		catch (ParserConfigurationException | SAXException | IOException ex)
		{
			Logger.getLogger(JFrameMain.class.getName()).log(Level.SEVERE, null, ex);
		}
		
	}
	
	private void storeToXML(String fileName)
	{
		try
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			Document doc = docBuilder.newDocument();
			Element dataset = doc.createElement("dataset");
			doc.appendChild(dataset);
			
			Element calc = doc.createElement("calculator");
			if(this.method == Calculator.DHONDT_METHOD)
				calc.setAttribute("method", "DHONDT");
			else if(this.method == Calculator.HARE_NIEMEYER_METHOD)
				calc.setAttribute("method", "HARE-NIEMEYER");
			else if(this.method == Calculator.SAINT_LAGUE_METHOD)
				calc.setAttribute("mthod", "SAINT-LAGUE");
			dataset.appendChild(calc);
			calc.setAttribute("seats", this.seats + "");
			calc.setAttribute("coalTh", this.coalThresh + "");
			calc.setAttribute("partyTh", this.partyThresh + "");
			calc.setAttribute("indivTh", this.indivThresh + "");
			
			for(Subject sub : this.subjects)
			{
				Element subject = doc.createElement("subject");
				dataset.appendChild(subject);
				if(sub.getCategory() == Subject.COALITION)
					subject.setAttribute("category", "COALITION");
				else if(sub.getCategory() == Subject.PARTY)
					subject.setAttribute("category", "PARTY");
				else if(sub.getCategory() == Subject.INDIVIDUAL)
					subject.setAttribute("category", "INDIVIDUAL");
				subject.setAttribute("name", sub.getName());
				subject.setAttribute("votes", sub.getVotes() + "");
			}
			
			//write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(fileName));
			transformer.transform(source, result);
		}
		catch (ParserConfigurationException | TransformerException ex)
		{
			Logger.getLogger(JFrameMain.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	@SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents()
     {
          java.awt.GridBagConstraints gridBagConstraints;

          buttonGroup1 = new javax.swing.ButtonGroup();
          buttonGroup2 = new javax.swing.ButtonGroup();
          jPanel1 = new javax.swing.JPanel();
          jPanel2 = new javax.swing.JPanel();
          jLabel2 = new javax.swing.JLabel();
          jSpinnerTotSeats = new javax.swing.JSpinner();
          jSeparator1 = new javax.swing.JSeparator();
          jLabel3 = new javax.swing.JLabel();
          jLabel1 = new javax.swing.JLabel();
          jRadioButtonMethodHN = new javax.swing.JRadioButton();
          jRadioButtonMethodDH = new javax.swing.JRadioButton();
          jRadioButtonMethodSL = new javax.swing.JRadioButton();
          jSeparator3 = new javax.swing.JSeparator();
          jPanel4 = new javax.swing.JPanel();
          jLabel4 = new javax.swing.JLabel();
          jSpinnerCoalThresh = new javax.swing.JSpinner();
          jLabel5 = new javax.swing.JLabel();
          jSpinnerPartyThresh = new javax.swing.JSpinner();
          jLabel6 = new javax.swing.JLabel();
          jSpinnerIndivThresh = new javax.swing.JSpinner();
          jSeparator5 = new javax.swing.JSeparator();
          jSeparator6 = new javax.swing.JSeparator();
          jPanel3 = new javax.swing.JPanel();
          jScrollPane1 = new javax.swing.JScrollPane();
          jTable1 = new javax.swing.JTable();
          jPanel5 = new javax.swing.JPanel();
          jLabel7 = new javax.swing.JLabel();
          jTextFieldSubjectName = new javax.swing.JTextField();
          jLabel8 = new javax.swing.JLabel();
          jFormattedTextFieldSubjectVotes = new javax.swing.JFormattedTextField();
          jLabel9 = new javax.swing.JLabel();
          jComboBox1 = new javax.swing.JComboBox();
          jButton1 = new javax.swing.JButton();
          jMenuBar1 = new javax.swing.JMenuBar();
          jMenu1 = new javax.swing.JMenu();
          jMenuItemRest = new javax.swing.JMenuItem();
          jSeparator4 = new javax.swing.JPopupMenu.Separator();
          jMenuItem1 = new javax.swing.JMenuItem();
          jMenuItem4 = new javax.swing.JMenuItem();
          jSeparator2 = new javax.swing.JPopupMenu.Separator();
          jMenuItem5 = new javax.swing.JMenuItem();
          jMenu2 = new javax.swing.JMenu();
          jMenuItem2 = new javax.swing.JMenuItem();
          jMenuItem3 = new javax.swing.JMenuItem();

          setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
          setExtendedState(JFrame.MAXIMIZED_BOTH);

          jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
          jPanel1.setLayout(new java.awt.GridBagLayout());

          jPanel2.setLayout(new java.awt.GridBagLayout());

          jLabel2.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
          jLabel2.setText("Tot. Seats (S)");
          gridBagConstraints = new java.awt.GridBagConstraints();
          gridBagConstraints.gridx = 0;
          gridBagConstraints.gridy = 0;
          gridBagConstraints.gridwidth = 3;
          jPanel2.add(jLabel2, gridBagConstraints);

          jSpinnerTotSeats.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
          jSpinnerTotSeats.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10000, 1));
          jSpinnerTotSeats.addChangeListener(new javax.swing.event.ChangeListener()
          {
               public void stateChanged(javax.swing.event.ChangeEvent evt)
               {
                    jSpinnerTotSeatsStateChanged(evt);
               }
          });
          jSpinnerTotSeats.addPropertyChangeListener(new java.beans.PropertyChangeListener()
          {
               public void propertyChange(java.beans.PropertyChangeEvent evt)
               {
                    jSpinnerTotSeatsPropertyChange(evt);
               }
          });
          gridBagConstraints = new java.awt.GridBagConstraints();
          gridBagConstraints.gridx = 0;
          gridBagConstraints.gridy = 1;
          gridBagConstraints.gridwidth = 3;
          jPanel2.add(jSpinnerTotSeats, gridBagConstraints);
          this.jSpinnerTotSeats.setValue(this.seats);

          jSeparator1.setBackground(new java.awt.Color(0, 0, 0));
          gridBagConstraints = new java.awt.GridBagConstraints();
          gridBagConstraints.gridx = 0;
          gridBagConstraints.gridy = 2;
          gridBagConstraints.gridwidth = 3;
          gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
          gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
          jPanel2.add(jSeparator1, gridBagConstraints);

          jLabel3.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
          jLabel3.setText("Thresholds (%)");
          gridBagConstraints = new java.awt.GridBagConstraints();
          gridBagConstraints.gridx = 0;
          gridBagConstraints.gridy = 3;
          gridBagConstraints.gridwidth = 3;
          gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
          jPanel2.add(jLabel3, gridBagConstraints);

          jLabel1.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
          jLabel1.setText("Method");
          gridBagConstraints = new java.awt.GridBagConstraints();
          gridBagConstraints.gridx = 0;
          gridBagConstraints.gridy = 6;
          gridBagConstraints.gridwidth = 3;
          gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
          jPanel2.add(jLabel1, gridBagConstraints);

          buttonGroup1.add(jRadioButtonMethodHN);
          jRadioButtonMethodHN.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
          jRadioButtonMethodHN.setText("Hare-Niemeyer");
          jRadioButtonMethodHN.setEnabled(false);
          gridBagConstraints = new java.awt.GridBagConstraints();
          gridBagConstraints.gridx = 0;
          gridBagConstraints.gridy = 7;
          jPanel2.add(jRadioButtonMethodHN, gridBagConstraints);
          this.jRadioButtonMethodHN.setSelected(this.method == JFrameMain.HARE_NEIMEYER);

          buttonGroup1.add(jRadioButtonMethodDH);
          jRadioButtonMethodDH.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
          jRadioButtonMethodDH.setSelected(true);
          jRadioButtonMethodDH.setText("D'Hondt");
          jRadioButtonMethodDH.addChangeListener(new javax.swing.event.ChangeListener()
          {
               public void stateChanged(javax.swing.event.ChangeEvent evt)
               {
                    jRadioButtonMethodDHStateChanged(evt);
               }
          });
          gridBagConstraints = new java.awt.GridBagConstraints();
          gridBagConstraints.gridx = 1;
          gridBagConstraints.gridy = 7;
          jPanel2.add(jRadioButtonMethodDH, gridBagConstraints);
          this.jRadioButtonMethodDH.setEnabled(this.method == JFrameMain.DHONDT);

          buttonGroup1.add(jRadioButtonMethodSL);
          jRadioButtonMethodSL.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
          jRadioButtonMethodSL.setText("Sainte-Laguë");
          jRadioButtonMethodSL.addChangeListener(new javax.swing.event.ChangeListener()
          {
               public void stateChanged(javax.swing.event.ChangeEvent evt)
               {
                    jRadioButtonMethodSLStateChanged(evt);
               }
          });
          gridBagConstraints = new java.awt.GridBagConstraints();
          gridBagConstraints.gridx = 2;
          gridBagConstraints.gridy = 7;
          jPanel2.add(jRadioButtonMethodSL, gridBagConstraints);
          this.jRadioButtonMethodSL.setSelected(this.method == JFrameMain.SAINTE_LAGUE);

          jSeparator3.setBackground(new java.awt.Color(0, 0, 0));
          gridBagConstraints = new java.awt.GridBagConstraints();
          gridBagConstraints.gridx = 0;
          gridBagConstraints.gridy = 5;
          gridBagConstraints.gridwidth = 3;
          gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
          gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
          jPanel2.add(jSeparator3, gridBagConstraints);

          jPanel4.setLayout(new java.awt.GridBagLayout());

          jLabel4.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
          jLabel4.setText("Coal. (cth)");
          gridBagConstraints = new java.awt.GridBagConstraints();
          gridBagConstraints.gridx = 0;
          gridBagConstraints.gridy = 4;
          gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
          jPanel4.add(jLabel4, gridBagConstraints);

          jSpinnerCoalThresh.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
          jSpinnerCoalThresh.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(100.0f), Float.valueOf(0.1f)));
          jSpinnerCoalThresh.addChangeListener(new javax.swing.event.ChangeListener()
          {
               public void stateChanged(javax.swing.event.ChangeEvent evt)
               {
                    jSpinnerCoalThreshStateChanged(evt);
               }
          });
          gridBagConstraints = new java.awt.GridBagConstraints();
          gridBagConstraints.gridx = 1;
          gridBagConstraints.gridy = 4;
          gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
          gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
          jPanel4.add(jSpinnerCoalThresh, gridBagConstraints);

          jLabel5.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
          jLabel5.setText("Party (pth)");
          gridBagConstraints = new java.awt.GridBagConstraints();
          gridBagConstraints.gridx = 2;
          gridBagConstraints.gridy = 4;
          gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
          jPanel4.add(jLabel5, gridBagConstraints);

          jSpinnerPartyThresh.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
          jSpinnerPartyThresh.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(100.0f), Float.valueOf(0.1f)));
          jSpinnerPartyThresh.addChangeListener(new javax.swing.event.ChangeListener()
          {
               public void stateChanged(javax.swing.event.ChangeEvent evt)
               {
                    jSpinnerPartyThreshStateChanged(evt);
               }
          });
          gridBagConstraints = new java.awt.GridBagConstraints();
          gridBagConstraints.gridx = 3;
          gridBagConstraints.gridy = 4;
          gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
          gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
          jPanel4.add(jSpinnerPartyThresh, gridBagConstraints);

          jLabel6.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
          jLabel6.setText("Indiv. (ith)");
          gridBagConstraints = new java.awt.GridBagConstraints();
          gridBagConstraints.gridx = 4;
          gridBagConstraints.gridy = 4;
          gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
          jPanel4.add(jLabel6, gridBagConstraints);

          jSpinnerIndivThresh.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
          jSpinnerIndivThresh.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(100.0f), Float.valueOf(0.1f)));
          jSpinnerIndivThresh.addChangeListener(new javax.swing.event.ChangeListener()
          {
               public void stateChanged(javax.swing.event.ChangeEvent evt)
               {
                    jSpinnerIndivThreshStateChanged(evt);
               }
          });
          gridBagConstraints = new java.awt.GridBagConstraints();
          gridBagConstraints.gridx = 5;
          gridBagConstraints.gridy = 4;
          gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
          jPanel4.add(jSpinnerIndivThresh, gridBagConstraints);

          gridBagConstraints = new java.awt.GridBagConstraints();
          gridBagConstraints.gridx = 0;
          gridBagConstraints.gridy = 4;
          gridBagConstraints.gridwidth = 3;
          gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
          gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
          jPanel2.add(jPanel4, gridBagConstraints);
          gridBagConstraints = new java.awt.GridBagConstraints();
          gridBagConstraints.gridx = 0;
          gridBagConstraints.gridy = 8;
          gridBagConstraints.gridwidth = 3;
          gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
          gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
          gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
          jPanel2.add(jSeparator5, gridBagConstraints);

          gridBagConstraints = new java.awt.GridBagConstraints();
          gridBagConstraints.gridx = 0;
          gridBagConstraints.gridy = 0;
          gridBagConstraints.gridwidth = 3;
          gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
          gridBagConstraints.weighty = 1.0;
          gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
          jPanel1.add(jPanel2, gridBagConstraints);

          jSeparator6.setBackground(new java.awt.Color(0, 0, 0));
          jSeparator6.setOrientation(javax.swing.SwingConstants.VERTICAL);
          gridBagConstraints = new java.awt.GridBagConstraints();
          gridBagConstraints.gridx = 3;
          gridBagConstraints.gridy = 0;
          gridBagConstraints.gridheight = 2;
          gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
          jPanel1.add(jSeparator6, gridBagConstraints);

          getContentPane().add(jPanel1, java.awt.BorderLayout.LINE_START);

          jPanel3.addKeyListener(new java.awt.event.KeyAdapter()
          {
               public void keyTyped(java.awt.event.KeyEvent evt)
               {
                    jPanel3KeyTyped(evt);
               }
          });

          jScrollPane1.setBorder(null);

          jTable1.setAutoCreateRowSorter(true);
          jTable1.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
          jTable1.setModel(new javax.swing.table.DefaultTableModel(
               new Object [][]
               {

               },
               new String []
               {
                    "Subject", "Category", "Votes", "%", "Seats"
               }
          ));
          jTable1.setGridColor(new java.awt.Color(0, 255, 0));
          jTable1.setRowHeight(25);
          jTable1.setSelectionBackground(new java.awt.Color(255, 255, 204));
          jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
          jTable1.setShowVerticalLines(false);
          jTable1.addMouseListener(new java.awt.event.MouseAdapter()
          {
               public void mouseClicked(java.awt.event.MouseEvent evt)
               {
                    jTable1MouseClicked(evt);
               }
          });
          jScrollPane1.setViewportView(jTable1);

          jLabel7.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
          jLabel7.setText("Name");
          jPanel5.add(jLabel7);

          jTextFieldSubjectName.setColumns(20);
          jTextFieldSubjectName.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
          jPanel5.add(jTextFieldSubjectName);

          jLabel8.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
          jLabel8.setText("Votes");
          jPanel5.add(jLabel8);

          jFormattedTextFieldSubjectVotes.setColumns(5);
          jFormattedTextFieldSubjectVotes.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
          jFormattedTextFieldSubjectVotes.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
          jFormattedTextFieldSubjectVotes.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
          jFormattedTextFieldSubjectVotes.setValue(0);
          jPanel5.add(jFormattedTextFieldSubjectVotes);

          jLabel9.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
          jLabel9.setText("Category");
          jPanel5.add(jLabel9);

          jComboBox1.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
          jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Coalition", "Party", "Individual" }));
          jComboBox1.addActionListener(new java.awt.event.ActionListener()
          {
               public void actionPerformed(java.awt.event.ActionEvent evt)
               {
                    jComboBox1ActionPerformed(evt);
               }
          });
          jPanel5.add(jComboBox1);

          jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/seatscalculator/1435978364_ic_add_circle_outline_48px.png"))); // NOI18N
          jButton1.setFocusCycleRoot(true);
          jButton1.addActionListener(new java.awt.event.ActionListener()
          {
               public void actionPerformed(java.awt.event.ActionEvent evt)
               {
                    jButton1ActionPerformed(evt);
               }
          });
          jPanel5.add(jButton1);

          javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
          jPanel3.setLayout(jPanel3Layout);
          jPanel3Layout.setHorizontalGroup(
               jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(jPanel3Layout.createSequentialGroup()
                    .addGap(0, 0, 0)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                         .addComponent(jScrollPane1)
                         .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 598, Short.MAX_VALUE))
                    .addGap(0, 0, 0))
          );
          jPanel3Layout.setVerticalGroup(
               jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(jPanel3Layout.createSequentialGroup()
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, 0)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 523, Short.MAX_VALUE)
                    .addGap(0, 0, 0))
          );

          getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

          jMenu1.setText("File");

          jMenuItemRest.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
          jMenuItemRest.setIcon(new javax.swing.ImageIcon(getClass().getResource("/seatscalculator/1435977702_ic_refresh_48px.png"))); // NOI18N
          jMenuItemRest.setText("Reset");
          jMenuItemRest.addActionListener(new java.awt.event.ActionListener()
          {
               public void actionPerformed(java.awt.event.ActionEvent evt)
               {
                    jMenuItemRestActionPerformed(evt);
               }
          });
          jMenu1.add(jMenuItemRest);
          jMenu1.add(jSeparator4);

          jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
          jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/seatscalculator/1435977789_ic_keyboard_arrow_up_48px.png"))); // NOI18N
          jMenuItem1.setText("Import");
          jMenuItem1.addActionListener(new java.awt.event.ActionListener()
          {
               public void actionPerformed(java.awt.event.ActionEvent evt)
               {
                    jMenuItem1ActionPerformed(evt);
               }
          });
          jMenu1.add(jMenuItem1);

          jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
          jMenuItem4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/seatscalculator/1435977795_ic_keyboard_arrow_down_48px.png"))); // NOI18N
          jMenuItem4.setText("Export");
          jMenuItem4.addActionListener(new java.awt.event.ActionListener()
          {
               public void actionPerformed(java.awt.event.ActionEvent evt)
               {
                    jMenuItem4ActionPerformed(evt);
               }
          });
          jMenu1.add(jMenuItem4);
          jMenu1.add(jSeparator2);

          jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0));
          jMenuItem5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/seatscalculator/1435977914_ic_exit_to_app_48px.png"))); // NOI18N
          jMenuItem5.setText("Exit");
          jMenuItem5.addActionListener(new java.awt.event.ActionListener()
          {
               public void actionPerformed(java.awt.event.ActionEvent evt)
               {
                    jMenuItem5ActionPerformed(evt);
               }
          });
          jMenu1.add(jMenuItem5);

          jMenuBar1.add(jMenu1);

          jMenu2.setText("?");

          jMenuItem2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/seatscalculator/1435978190_ic_language_48px.png"))); // NOI18N
          jMenuItem2.setText("Help");
          jMenuItem2.addActionListener(new java.awt.event.ActionListener()
          {
               public void actionPerformed(java.awt.event.ActionEvent evt)
               {
                    jMenuItem2ActionPerformed(evt);
               }
          });
          jMenu2.add(jMenuItem2);

          jMenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/seatscalculator/1435978042_ic_info_outline_48px.png"))); // NOI18N
          jMenuItem3.setText("About");
          jMenuItem3.addActionListener(new java.awt.event.ActionListener()
          {
               public void actionPerformed(java.awt.event.ActionEvent evt)
               {
                    jMenuItem3ActionPerformed(evt);
               }
          });
          jMenu2.add(jMenuItem3);

          jMenuBar1.add(jMenu2);

          setJMenuBar(jMenuBar1);

          pack();
     }// </editor-fold>//GEN-END:initComponents

     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
     {//GEN-HEADEREND:event_jButton1ActionPerformed

			String name = this.jTextFieldSubjectName.getText().length() == 0 ? "Subject " + this.subjects.size() : this.jTextFieldSubjectName.getText();
			int category = this.selectedCategory;
			int votes = Integer.parseInt(this.jFormattedTextFieldSubjectVotes.getText());
			this.addSubject(new Subject(
				   category, 
				   name, 
				   votes)
			);
			this.jTextFieldSubjectName.setText("");
			this.jFormattedTextFieldSubjectVotes.setValue(0);
			this.jComboBox1.setSelectedIndex(0);
     }//GEN-LAST:event_jButton1ActionPerformed

	private int selectedCategory = Subject.COALITION;
     private void jSpinnerTotSeatsPropertyChange(java.beans.PropertyChangeEvent evt)//GEN-FIRST:event_jSpinnerTotSeatsPropertyChange
     {//GEN-HEADEREND:event_jSpinnerTotSeatsPropertyChange
          this.seats = (int) this.jSpinnerTotSeats.getValue();
		this.calculateVotingResults();
     }//GEN-LAST:event_jSpinnerTotSeatsPropertyChange

     private void jSpinnerTotSeatsStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerTotSeatsStateChanged
     {//GEN-HEADEREND:event_jSpinnerTotSeatsStateChanged
          this.seats = (int) this.jSpinnerTotSeats.getValue();
		this.calculateVotingResults();
     }//GEN-LAST:event_jSpinnerTotSeatsStateChanged

     private void jTable1MouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_jTable1MouseClicked
     {//GEN-HEADEREND:event_jTable1MouseClicked
          if(evt.getClickCount()!=2)
			return;
		
		int idx = this.jTable1.getSelectedRow();
		if(idx<0)
			return;
		
		this.subjects.remove(idx);
		this.calculateVotingResults();
     }//GEN-LAST:event_jTable1MouseClicked

     private void jPanel3KeyTyped(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jPanel3KeyTyped
     {//GEN-HEADEREND:event_jPanel3KeyTyped
          if(evt.getKeyCode()!=KeyEvent.VK_ENTER)
			return;
		this.jButton1ActionPerformed(null);
     }//GEN-LAST:event_jPanel3KeyTyped

     private void jRadioButtonMethodDHStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jRadioButtonMethodDHStateChanged
     {//GEN-HEADEREND:event_jRadioButtonMethodDHStateChanged
          if(this.jRadioButtonMethodDH.isSelected())
			this.method = Calculator.DHONDT_METHOD;
		this.calculateVotingResults();
     }//GEN-LAST:event_jRadioButtonMethodDHStateChanged

     private void jRadioButtonMethodSLStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jRadioButtonMethodSLStateChanged
     {//GEN-HEADEREND:event_jRadioButtonMethodSLStateChanged
          if(this.jRadioButtonMethodSL.isSelected())
			this.method = Calculator.SAINT_LAGUE_METHOD;
		this.calculateVotingResults();
     }//GEN-LAST:event_jRadioButtonMethodSLStateChanged

     private void jSpinnerCoalThreshStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerCoalThreshStateChanged
     {//GEN-HEADEREND:event_jSpinnerCoalThreshStateChanged
          this.coalThresh = (float) this.jSpinnerCoalThresh.getValue();
		this.calculateVotingResults();
     }//GEN-LAST:event_jSpinnerCoalThreshStateChanged

     private void jSpinnerPartyThreshStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerPartyThreshStateChanged
     {//GEN-HEADEREND:event_jSpinnerPartyThreshStateChanged
          this.partyThresh = (float) this.jSpinnerPartyThresh.getValue();
		this.calculateVotingResults();
     }//GEN-LAST:event_jSpinnerPartyThreshStateChanged

     private void jSpinnerIndivThreshStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerIndivThreshStateChanged
     {//GEN-HEADEREND:event_jSpinnerIndivThreshStateChanged
          this.indivThresh = (float) this.jSpinnerIndivThresh.getValue();
		this.calculateVotingResults();
     }//GEN-LAST:event_jSpinnerIndivThreshStateChanged

     private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jComboBox1ActionPerformed
     {//GEN-HEADEREND:event_jComboBox1ActionPerformed
          this.selectedCategory = this.jComboBox1.getSelectedIndex();
     }//GEN-LAST:event_jComboBox1ActionPerformed

     private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem1ActionPerformed
     {//GEN-HEADEREND:event_jMenuItem1ActionPerformed
          
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("XML Files", "xml");
		chooser.setFileFilter(filter);
		File workingDirectory = new File(System.getProperty("user.dir"));
		chooser.setCurrentDirectory(workingDirectory);
		int returnVal = chooser.showOpenDialog(this);
		if(returnVal != JFileChooser.APPROVE_OPTION)
		   return;
		String fileName = chooser.getSelectedFile().getAbsolutePath();
		this.loadFromXML(fileName);
     }//GEN-LAST:event_jMenuItem1ActionPerformed

     private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem4ActionPerformed
     {//GEN-HEADEREND:event_jMenuItem4ActionPerformed
          JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("XML Files", "xml");
		chooser.setFileFilter(filter);
		File workingDirectory = new File(System.getProperty("user.dir")  + "\\" + System.currentTimeMillis() + ".xml");
		chooser.setSelectedFile(workingDirectory);
		int returnVal = chooser.showSaveDialog(this);
		if(returnVal != JFileChooser.APPROVE_OPTION)
		   return;
		String fileName = chooser.getSelectedFile().getAbsolutePath();
		this.storeToXML(fileName);
     }//GEN-LAST:event_jMenuItem4ActionPerformed

     private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem5ActionPerformed
     {//GEN-HEADEREND:event_jMenuItem5ActionPerformed
          exitOp();
     }//GEN-LAST:event_jMenuItem5ActionPerformed

     private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem3ActionPerformed
     {//GEN-HEADEREND:event_jMenuItem3ActionPerformed
          JOptionPane.showMessageDialog(this, new JPanelAbout(), "About", JOptionPane.PLAIN_MESSAGE);
     }//GEN-LAST:event_jMenuItem3ActionPerformed

     private void jMenuItemRestActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemRestActionPerformed
     {//GEN-HEADEREND:event_jMenuItemRestActionPerformed
          this.seats = 0; this.jSpinnerTotSeats.setValue(this.seats);
		this.coalThresh = 0; this.jSpinnerCoalThresh.setValue(this.coalThresh);
		this.partyThresh = 0; this.jSpinnerPartyThresh.setValue(this.partyThresh);
		this.indivThresh = 0; this.jSpinnerIndivThresh.setValue(this.indivThresh);
		this.jRadioButtonMethodDH.setSelected(true);
		this.jRadioButtonMethodHN.setSelected(false);
		this.jRadioButtonMethodSL.setSelected(false);
		this.jTextFieldSubjectName.setText("");
		this.jFormattedTextFieldSubjectVotes.setValue(0);
		this.jComboBox1.setSelectedIndex(0);
		this.subjects = new ArrayList();
		this.calculateVotingResults();
     }//GEN-LAST:event_jMenuItemRestActionPerformed

     private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem2ActionPerformed
     {//GEN-HEADEREND:event_jMenuItem2ActionPerformed
          if(Desktop.isDesktopSupported())
		{
			try
			{
				Desktop.getDesktop().browse(new URI("https://infokomshknr1.wordpress.com/"));
			}
			catch (URISyntaxException | IOException ex)
			{
				Logger.getLogger(JFrameMain.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
     }//GEN-LAST:event_jMenuItem2ActionPerformed
	
	public static void main(String args[])
	{
		try
		{
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
			{
				if ("System".equals(info.getName()))
				{
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex)
		{
			java.util.logging.Logger.getLogger(JFrameMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		
		java.awt.EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				new JFrameMain().setVisible(true);
			}
		});
	}

	// <editor-fold defaultstate="collapsed" desc="Varibales Declaration">   
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.ButtonGroup buttonGroup1;
     private javax.swing.ButtonGroup buttonGroup2;
     private javax.swing.JButton jButton1;
     private javax.swing.JComboBox jComboBox1;
     private javax.swing.JFormattedTextField jFormattedTextFieldSubjectVotes;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JMenu jMenu1;
     private javax.swing.JMenu jMenu2;
     private javax.swing.JMenuBar jMenuBar1;
     private javax.swing.JMenuItem jMenuItem1;
     private javax.swing.JMenuItem jMenuItem2;
     private javax.swing.JMenuItem jMenuItem3;
     private javax.swing.JMenuItem jMenuItem4;
     private javax.swing.JMenuItem jMenuItem5;
     private javax.swing.JMenuItem jMenuItemRest;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JRadioButton jRadioButtonMethodDH;
     private javax.swing.JRadioButton jRadioButtonMethodHN;
     private javax.swing.JRadioButton jRadioButtonMethodSL;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JPopupMenu.Separator jSeparator2;
     private javax.swing.JSeparator jSeparator3;
     private javax.swing.JPopupMenu.Separator jSeparator4;
     private javax.swing.JSeparator jSeparator5;
     private javax.swing.JSeparator jSeparator6;
     private javax.swing.JSpinner jSpinnerCoalThresh;
     private javax.swing.JSpinner jSpinnerIndivThresh;
     private javax.swing.JSpinner jSpinnerPartyThresh;
     private javax.swing.JSpinner jSpinnerTotSeats;
     private javax.swing.JTable jTable1;
     private javax.swing.JTextField jTextFieldSubjectName;
     // End of variables declaration//GEN-END:variables
	// </editor-fold>
}
