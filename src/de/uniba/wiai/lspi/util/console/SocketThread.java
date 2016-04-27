/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uniba.wiai.lspi.util.console;

import de.uniba.wiai.lspi.util.console.parser.CommandParser;
import de.uniba.wiai.lspi.util.console.parser.ParseException;
import de.uniba.wiai.lspi.util.console.parser.TokenMgrError;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 *
 * @author User1
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.OutputStream;

import de.uniba.wiai.lspi.util.console.parser.CommandParser;
import de.uniba.wiai.lspi.util.console.parser.ParseException;
import de.uniba.wiai.lspi.util.console.parser.TokenMgrError;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author sven
 * @version 1.0.5
 */

/*
added by MNNIT pratyush and vasu
*/
public class SocketThread extends Thread {

	/**
	 * Reference to the only instance of this.
	 */
	static SocketThread queryingSocket;

	/**
	 * The name of this console. Displayed before any input.
	 */
	protected String consoleName;

	/**
	 * The factory responsible for Creation of commands.
	 */
	protected CommandFactory factory;

	/**
	 * The text displayed, when the Thread is started.
	 */
	protected String welcome = "Welcome!";

	/**
	 * The {@link Command} that exits the console. Its execute method is invoked
	 * and the console exits.
	 */
	protected Command exitCommand;

	/**
	 * The PrintStream to print the consoles output to.
	 */
	protected PrintStream out;

	/**
	 * The old PrintStream to print the standard output to. Is <code>null</code>
	 * if this console does not change the standard output via constructor
	 * {@link #ConsoleThread(String, CommandFactory)}.
	 */
	protected PrintStream systemOut = null;

	/**
	 * Reference to the standard output stream.
	 */
	protected OutputStream systemOutputStream = null;

	/**
	 * Creates a new instance of ConsoleThread
	 *
	 * @param name
	 *            The name of this console. Displayed before any input.
	 * @param f
	 *            The {@link CommandFactory} responsible for creating
	 *            {@link Command}s.
	 */
	protected SocketThread(String name, CommandFactory f) {
		super("Console-" + name);
		this.consoleName = name;
		this.factory = f;
		this.out = f.getPrintStream();
	}

	/**
	 * Creates a new instance of ConsoleThread. The standard output
	 * {@link System#out} is redirected to <code>systemOut</code>.
	 *
	 * @param name
	 *            The name of this console. Displayed before any input.
	 * @param f
	 *            The {@link CommandFactory} responsible for creating
	 *            {@link Command}s.
	 * @param systemOut
	 *            The {@link OutputStream} to redirect standard ouput to. If you
	 *            do not want to have any output send to System.out, you can use
	 *            {@link DummyOutputStream}. If you want to save output send to
	 *            System.out in Memory to e.g. display it later you can use
	 *            {@link MemoryOutputStream}. To write output to a file you can
	 *            use {@link java.io.FileOutputStream}.
	 */
	protected SocketThread(String name, CommandFactory f,
			OutputStream systemOut) {
		this(name, f);
		/* save System.out to restore it when this console thread is stopped. */
		this.systemOut = System.out;
		this.systemOutputStream = systemOut;
		System.setOut(new PrintStream(this.systemOutputStream));
	}

	/**
	 * Method to obtain a reference to the console currently active in this JVM.
	 * Returns <code>null</code>, if there is none.
	 *
	 * @return Reference to the singleton console thread.
	 */
	public static SocketThread getConsole() {
		return queryingSocket;
	}

	/**
	 * Factory method to get a reference to the console singleton. Creates a new
	 * instance if there is no console in the JVM. If there is currently one
	 * console the arguments provided to this method have no effect.
	 *
	 * @param name
	 * @param factory
	 * @return Reference to the singleton console thread.
	 */
	public static SocketThread getConsole(String name, CommandFactory factory) {
		if (queryingSocket == null) {
			queryingSocket = new SocketThread(name, factory);
		}
		return queryingSocket;
	}

	/**
	 * Factory method to create a console. Creates a new instance if there is no
	 * console in the JVM. If there is currently one console the arguments
	 * provided to this method have no effect.
	 *
	 * @param name
	 * @param factory
	 * @param systemOut
	 * @return Reference to the singleton console thread.
	 */
	public static SocketThread getConsole(String name, CommandFactory factory,
			OutputStream systemOut) {
		if (queryingSocket == null) {
			queryingSocket = new SocketThread(name, factory, systemOut);
		}
		return queryingSocket;
	}

	/**
	 * Get a reference to the {@link PrintStream} this console prints its output
	 * to.
	 *
	 * @return Reference to the {@link PrintStream} this console prints its
	 *         output to.
	 */
	public PrintStream getPrintStream() {
		return this.out;
	}

	/**
	 * Get a reference to the {@link OutputStream} calls to System.out are
	 * delegated to. Returns <code>null</code> if System.out has not been
	 * redirected.
	 *
	 * @return Reference to the {@link OutputStream} calls to System.out are
	 *         delegated to. Returns <code>null</code> if System.out has not
	 *         been redirected.
	 */
	public OutputStream getSystemOutputStream() {
		return this.systemOutputStream;
	}

	/**
	 * Get a reference to the {@link CommandFactory} used by this console.
	 *
	 * @return Reference to the {@link CommandFactory} used by this console.
	 */
	public CommandFactory getCommandFactory() {
		return this.factory;
	}

	/**
	 * Set a costum welcome text for the console.
	 *
	 * @param text
	 *            The welcome text to set.
	 */
 	public void setWelcomeText(String text) {
		this.welcome = text;
	}

	/**
	 * The run method. Loops until the exitCommand has been invoked.
	 */
	public void run() {
            try {
                this.out.println(this.welcome);
                boolean running = true;
                this.out.println("Console ready. ");
                //socket started
                ServerSocket serverSocket = new ServerSocket(1069);;
                while (running) {
                    this.out.print(this.consoleName + " > ");
                    try {
                        // added socket input

                        Socket clientSocket = serverSocket.accept();
                        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                        String command = in.readLine();
                        this.out.println(command + "recieved ...");
                        DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                        //socket input ends
                        String s="";
                          //added system.out redirected to array of bytes which can be read off
                         PrintStream originalOut= this.out;
                            ByteArrayOutputStream baos=new ByteArrayOutputStream();
                        PrintStream newout=new PrintStream(baos);
                        this.out = newout;
                        //this.out.print("X");
                        // addition ends

                        if (command == null) {
                            command = "";
                        }
                        command = command.trim();

                        if (!command.equals("")) {
                            String c = CommandParser.parse(command);
                            if ((this.exitCommand != null)
                                    && (c.equalsIgnoreCase(this.exitCommand
                                            .getCommandName()))) {
                                this.out.println("Exiting " + this.consoleName + "...");
                                Command com = this.factory.createCommand(command);
                                com.setPrintStream(this.out);
                                this.out.print("Do you really want to shutdown? ");
                                String answer = "";
                                while ((answer == null) || (answer.length() == 0)) {
                                    try {
                                        answer = in.readUTF();
                                    } catch (IOException e) {
                                        answer = "";
                                    }
                                    if ( (answer != null) && ((answer.equalsIgnoreCase("Yes"))
                                            || (answer.equalsIgnoreCase("Y")))) {
                                        com.execute();
                                        running = false;
                                    }

                                }
                            } else {
                                try {
                                    
                                    Command com = this.factory.createCommand(command);
                                    com.setPrintStream(this.out);
                                    com.execute();

                                } catch (ConsoleException e) {
                                    this.out.println(e.getMessage());
                                    // e.printStackTrace(out);
                                }
                            }
                        }


                    // added restoration of stream
                    this.out.flush();
                    this.out = originalOut; // So you can print agai
                    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                    this.out.println(new String(baos.toByteArray()));
                    //restoration of stream done

                    // added sending of data
                     s="";
                    /*char c;
                    while( (c=(char)bais.read()) != -1 )
                    {
                        s+=c;
                    }*/
                    s=new String(baos.toByteArray());
                    this.out.println(s);
                    //restoration of stream done
                    //  out.writeChars(s.length()+"\n");
                    dos.writeUTF(s+"-");
                    clientSocket.close();
                    //serverSocket = new ServerSocket(1069);
                    // done with the sending of data
                    } catch (TokenMgrError tme) {
                        this.out.println("Could not parse command.");
                        this.out.println(tme.getMessage());
                    } catch (ParseException pe) {
                        this.out.println("Could not parse command.");
                        this.out.println(pe.getMessage());
                    } catch (Throwable t) {
                        t.printStackTrace();
                        this.out
                                .println("An unexpected Exception occured. Could not execute command. Reason: ");
                        this.out.println(t.getMessage());
                    }

                }
                this.out.println("Shutting down.");
                /* restore System.out */
                System.setOut(System.out);

            } catch (IOException ex) {
                Logger.getLogger(SocketThread.class.getName()).log(Level.SEVERE, null, ex);
            }
	}

	/**
	 * Set the {@link Command} that exits this console. Uses the
	 * {@link CommandFactory} to create an instance of the Command.
	 *
	 * @param commandName
	 *            The name of the command.
	 * @throws ConsoleException
	 *             Exception during creation of the command.
	 */
	public void setExitCommand(String commandName) throws ConsoleException {
		this.exitCommand = this.factory.createCommand(commandName);
	}

}

