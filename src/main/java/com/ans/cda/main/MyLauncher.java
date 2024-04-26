package com.ans.cda.main;

import com.ans.cda.ihm.WebViewSample;

/**
 * Main principal de l'application
 * 
 * @author bensa Nizar
 */
public final class MyLauncher {

	/**
	 * MyLauncher constructor
	 */
	private MyLauncher() {
		// empty constructor
	}

	/**
	 * void main solution de contournement pour que le compilateur java (après la
	 * version 1.9) prend en charge le javaFX
	 * 
	 * @param args
	 */
	public static void main(final String[] args) {
		WebViewSample.main(args);
	}
}
