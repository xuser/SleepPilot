package help;

/*
 * Copyright (C) 2010  Preston Lacey http://javaflacencoder.sourceforge.net/
 * All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

/******************************************************************************
 *                                                                            *
 * Copyright (c) 1999-2003 Wimba S.A., All Rights Reserved.                   *
 *                                                                            *
 * COPYRIGHT:                                                                 *
 *      This software is the property of Wimba S.A.                           *
 *      This software is redistributed under the Xiph.org variant of          *
 *      the BSD license.                                                      *
 *      Redistribution and use in source and binary forms, with or without    *
 *      modification, are permitted provided that the following conditions    *
 *      are met:                                                              *
 *      - Redistributions of source code must retain the above copyright      *
 *      notice, this list of conditions and the following disclaimer.         *
 *      - Redistributions in binary form must reproduce the above copyright   *
 *      notice, this list of conditions and the following disclaimer in the   *
 *      documentation and/or other materials provided with the distribution.  *
 *      - Neither the name of Wimba, the Xiph.org Foundation nor the names of *
 *      its contributors may be used to endorse or promote products derived   *
 *      from this software without specific prior written permission.         *
 *                                                                            *
 * WARRANTIES:                                                                *
 *      This software is made available by the authors in the hope            *
 *      that it will be useful, but without any warranty.                     *
 *      Wimba S.A. is not liable for any consequence related to the           *
 *      use of the provided software.                                         *
 *                                                                            *
 * Class: Lsp.java                                                            *
 *                                                                            *
 * Author: James LAWRENCE                                                     *
 * Modified by: Marc GIMPEL                                                   *
 * Based on code by: Jean-Marc VALIN                                          *
 *                                                                            *
 * Date: March 2003                                                           *
 *                                                                            *
 ******************************************************************************/

/* 
Original copyright
  FILE........: AKSLSPD.C
  TYPE........: Turbo C
  COMPANY.....: Voicetronix
  AUTHOR......: David Rowe
  DATE CREATED: 24/2/93

Modified by Jean-Marc Valin

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions
   are met:
   
   - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
   
   - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
   
   - Neither the name of the Xiph.org Foundation nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.
   
   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
   A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR
   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
   PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
   LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
   NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/**
 * This class is used to calculate LPC Coefficients for a FLAC stream.
 * I created an adapted version of the above named ones.
 * 
 * @author Preston Lacey
 * @author Jim Lawrence, helloNetwork.com
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 * @author Nils Finke
 */
public class LPC {
	/** The error calculated by the LPC algorithm */
	protected double rawError;
	/** The coefficients as calculated by the LPC algorithm */
	protected double[] rawCoefficients;
	private final double[] tempCoefficients;
	/** The order of this LPC calculation */
	protected int order;
	
    /* Operations counters. Not needed in a real application. */
    static int mpy=0;
    static int add=0;
    static int ptr=0;

	/**
	 * Constructor creates an LPC object of the given order.
	 * 
	 * @param order
	 *            Order for this LPC calculation.
	 */
	public LPC(int order) {
		this.order = order;
		rawError = 0;
		rawCoefficients = new double[order + 1];
		tempCoefficients = new double[order + 1];
	}

	/**
	 * Get this LPC object's order
	 * 
	 * @return order used for this LPC calculation.
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * Get the error for this LPC calculation
	 * 
	 * @return lpc error
	 */
	public double getError() {
		return rawError;
	}

	/**
	 * Get the calculated LPC Coefficients as an array.
	 * 
	 * @return lpc coefficients in an array.
	 */
	public double[] getCoefficients() {
		return rawCoefficients;
	}

	/**
	 * Calculate an LPC using the given Auto-correlation data. Static method
	 * used since this is slightly faster than a more strictly object-oriented
	 * approach.
	 * 
	 * @param lpc
	 *            LPC to calculate
	 * @param R
	 *            Autocorrelation data to use
	 */
	public static void calculate(LPC lpc, long[] R) {
		int coeffCount = lpc.order;

		// calculate first iteration directly
		double[] A = lpc.rawCoefficients;
		for (int i = 0; i < coeffCount + 1; i++)
			A[i] = 0.0;
		A[0] = 1;
		double E = R[0];

		// calculate remaining iterations

		if (R[0] == 0) {
			for (int i = 0; i < coeffCount + 1; i++)
				A[i] = 0.0;
		} else {
			double[] ATemp = lpc.tempCoefficients;
			for (int i = 0; i < coeffCount + 1; i++)
				ATemp[i] = 0.0;

			for (int k = 0; k < coeffCount; k++) {
				double lambda = 0.0;
				double temp = 0;
				for (int j = 0; j <= k; j++) {
					temp += A[j] * R[k + 1 - j];
				}
				lambda = -temp / E;

				for (int i = 0; i <= k + 1; i++) {
					ATemp[i] = A[i] + lambda * A[k + 1 - i];
				}
				System.arraycopy(ATemp, 0, A, 0, coeffCount + 1);
				E = (1 - lambda * lambda) * E;
			}
		}
		lpc.rawError = E;
	}

	/**
	 * Calculate an LPC using a prior order LPC's values to save calculations.
	 * 
	 * @param lpc
	 *            LPC to calculate
	 * @param R
	 *            Auto-correlation data to use.
	 * @param priorLPC
	 *            Prior order LPC to use(may be any order lower than our target
	 *            LPC)
	 */
	public static void calculateFromPrior(LPC lpc, long[] R, LPC priorLPC) {
		int coeffCount = lpc.order;

		// calculate first iteration directly
		double[] A = lpc.rawCoefficients;
		for (int i = 0; i < coeffCount + 1; i++)
			A[i] = 0.0;
		A[0] = 1;
		double E = R[0];
		int startIter = 0;
		if (priorLPC != null && priorLPC.order < lpc.order) {
			startIter = priorLPC.order;
			E = priorLPC.rawError;
			System.arraycopy(priorLPC.rawCoefficients, 0, A, 0, startIter + 1);
		}
		// calculate remaining iterations
		if (R[0] == 0) {
			for (int i = 0; i < coeffCount + 1; i++)
				A[i] = 0.0;
		} else {
			double[] ATemp = lpc.tempCoefficients;
			for (int i = 0; i < coeffCount + 1; i++)
				ATemp[i] = 0.0;

			for (int k = startIter; k < coeffCount; k++) {
				double lambda = 0.0;
				double temp = 0.0;
				for (int j = 0; j <= k; j++) {
					temp -= A[j] * R[k - j + 1];
				}
				lambda = temp / E;

				for (int i = 0; i <= k + 1; i++) {
					ATemp[i] = A[i] + lambda * A[k + 1 - i];
				}
				System.arraycopy(ATemp, 0, A, 0, coeffCount + 1);
				E = (1 - lambda * lambda) * E;
			}
		}
		lpc.rawError = E;
	}

	/**
	 * Create auto-correlation coefficients(up to a maxOrder of 32).
	 * 
	 * @param R
	 *            Array to put results in.
	 * @param samples
	 *            Samples to calculate the auto-correlation for.
	 * @param count
	 *            number of samples to use
	 * @param start
	 *            index of samples array to start at
	 * @param increment
	 *            number of indices to increment between valid samples(for
	 *            interleaved arrays)
	 * @param maxOrder
	 *            maximum order to calculate.
	 */
	public static void createAutoCorrelation(long[] R, Double[] samples,
			int count, int start, int increment, int maxOrder) {
		if (increment == 1 && start == 0) {
			for (int i = 0; i <= maxOrder; i++) {
				R[i] = 0;
				long temp = 0;
				for (int j = 0; j < count - i; j++) {
					temp += samples[j] * samples[j + i];
				}
				R[i] += temp;
			}
		} else {
			for (int i = 0; i <= maxOrder; i++) {
				R[i] = 0;
				int baseIndex = increment * i;
				long temp = 0;
				int innerLimit = (count - i) * increment;
				for (int j = start; j < innerLimit; j += increment) {
					temp += samples[j] * samples[j + baseIndex];
				}
				R[i] += temp;
			}
		}
	}

	/**
	 * Apply a window function to sample data
	 * 
	 * @param samples
	 *            Samples to apply window to. Values in this array are left
	 *            unaltered.
	 * @param count
	 *            number of samples to use
	 * @param start
	 *            index of samples array to start at
	 * @param increment
	 *            number of indices to increment between valid samples(for
	 *            interleaved arrays)
	 * @param windowedSamples
	 *            array containing windowed values. Return values are
	 *            packed(increment of one).
	 */
	public static void window(int[] samples, int count, int start,
			int increment, int[] windowedSamples) {
		int[] values = windowedSamples;
		int loopCount = 0;
		float halfway = count / 2.0f;
		float hth = halfway * halfway;
		float windowCount = -halfway;
		int limit = count * increment + start;
		for (int i = start; i < limit; i += increment) {
			float innerCount = windowCount < 0 ? -windowCount : windowCount;
			windowCount++;
			// double val = 1.0-(double)(innerCount/halfway);
			float val = 1.0f - innerCount * innerCount / hth;
			double temp = (double) samples[i] * val;
			temp = temp > 0 ? temp + 0.5 : temp - 0.5;
			values[loopCount++] = (int) temp;
		}
	}
	

	/**
     * Convert filter coefficients to lsp coefficients.
     * @param oneMinusA A(z) = a0 - sum { ai * z^-i } . a0 = 1.
     * @param type which of the four methods for a2lsf conversion to perform
     * @return the lsf coefficients in the range 0 to 0.5,
     * as an array of doubles of length oneMinusA.length-1.
     */
    public static double[] lpc2lsf(double[] oneMinusA, int type)
    {
        int order = oneMinusA.length - 1;
        double[] g1 = new double[100];
        double[] g2 = new double[100];
        double[] g1r = new double[100];
        double[] g2r = new double[100];
        boolean even;
        int g1_order, g2_order;
        int orderd2;
        
        int i, j;
        int swap;
        double Factor;

        /* Compute the lengths of the x polynomials. */

        even = (order & 1) == 0;  /* True if order is even. */
        if(even) g1_order = g2_order = order/2;
        else {
            g1_order = (order+1)/2;
            g2_order = g1_order - 1;
            throw new IllegalArgumentException("Odd order not implemented yet");
        }

        /* Compute the first half of K & R F1 & F2 polynomials. */

        /* Compute half of the symmetric and antisymmetric polynomials. */
        /* Remove the roots at +1 and -1. */

        orderd2=(order+1)/2;
        g1[orderd2] = oneMinusA[0];
        for(i=1;i<=orderd2;i++) g1[g1_order-i] = oneMinusA[i]+oneMinusA[order+1-i];
        g2[orderd2] = oneMinusA[0];
        for(i=1;i<=orderd2;i++) g2[orderd2-i] = oneMinusA[i]-oneMinusA[order+1-i];

        if(even) {
            for(i=1; i<=orderd2;i++) g1[orderd2-i] -= g1[orderd2-i+1];
            for(i=1; i<=orderd2;i++) g2[orderd2-i] += g2[orderd2-i+1];
        } else {
            for(i=2; i<=orderd2;i++) g2[orderd2-i] += g2[orderd2-i+2];   /* Right? */
        }

        /* Convert into polynomials in cos(alpha) */

        if(type == 1) {
            //System.out.println("Implementing chebyshev reduction\n");
            cheby1(g1,g1_order);
            cheby1(g2,g2_order);
            Factor = 0.5;
        } else if(type == 2) {
            //System.out.println("Implementing first alternate chebyshev reduction\n");
            cheby2(g1,g1_order);
            cheby2(g2,g2_order);
            Factor = 0.5;
        } else if(type == 3) {
            //System.out.println("Implementing second alternate chebyshev reduction\n");
            cheby3(g1,g1_order);
            cheby3(g2,g2_order);
            Factor = 1.0;
        } else if(type == 4) {
            //System.out.println("Implementing DID reduction\n");
            kw(g1,g1_order);
            kw(g2,g2_order);
            Factor = 0.5;
        } else {
            throw new IllegalArgumentException("valid type values are 1 to 4.\n");
        }
        /* Print the polynomials to be reduced. */
        //for(i=0;i<=g1_order;i++) {
        //    System.out.printf("%3d: %14.6g", new Object[] {new Integer(i), new Double(g1[i])});
        //    if(i<=g2_order) System.out.printf(" %14.6g",new Object[] {new Double(g2[i])});
        //    System.out.println();
        //}

        /* Find the roots of the 2 even polynomials.*/

        cacm283(g1,g1_order,g1r);
        cacm283(g2,g2_order,g2r);

        /* Convert back to angular frequencies in the range 0 to 0.5 */
        double[] lsp = new double[order];
        for(i=0, j=0 ; ; ) {
            lsp[j++] = Math.acos(Factor * g1r[i])/(2*Math.PI);
            if(j >= order) break;
            lsp[j++] = Math.acos(Factor * g2r[i])/(2*Math.PI);
            if(j >= order) break;
            i++;
        }
        return lsp;
    }

    
    /* The transformation as proposed in the paper. */
    static void cheby1(double[] g, int ord) {
        int i, j;
        int k;

        for(i=2; i<= ord; i++) {
            for(j=ord; j > i; j--) {
                g[j-2] -= g[j];           add++;
            }
            g[j-2] -= 2.0*g[j];           mpy++; add++;
            /* for(k=0;k<=ord;k++) printf(" %6.3f",g[k]); printf("\n"); */
        }
    }

    /* An alternate transformation giving roots between -2 and +2. */
    static void cheby2(double[] g, int ord) {
        int i, j;
        int k;

        g[0] *= 0.5;                              mpy++;
        for(i=2; i<= ord; i++) {
            for(j=ord; j >= i; j--) {
                g[j-2] -= g[j];           add++;
            }
            g[i-1] *= 0.5;                    mpy++;
            /* for(k=0;k<=ord;k++) printf(" %6.3f",g[k]); printf("\n"); */
        }
        g[ord] *= 0.5;                    mpy++;
    }

    /* Another transformation giving roots between -1 and +1. */
    static void cheby3(double[] g, int ord) {
        int i, j;
        int k;

        g[0] *= 0.5;                              mpy++;
        for(i=2; i<= ord; i++) {
            for(j=ord; j >= i; j--) {
                g[j-2] -= g[j];           add++;
                g[j] += g[j];             add++;
            }
            /* for(k=0;k<=ord;k++) printf(" %6.3f",g[k]); printf("\n"); */
        }
    }
    
    /* A simple rootfinding algorithm, as published in the Collected Algorithms of*/
    /* The Association for Computing Machinery, CACM algorithm 283.               */
    /* It's basically a Newton iteration, that applies optimization steps to all  */
    /* root estimates together. It is stated to work for polynomials whose roots  */
    /* are all real and distinct.  I know of no proof of global convergence, but  */
    /* in practice it has always worked for the LSF rootfinding problem, although */
    /* there may be an initial period of wild divergence before it starts         */
    /* converging. */
    static void cacm283(
    double[] a,    /* Input array of coefficients. Length ord+1. */
    int ord,
    double[] r     /* Holds the found roots. */
    )
    {
        int i, k;
        double val, p, delta, error;
        double rooti;
        int swap;

        for(i=0; i<ord;i++) r[i] = 2.0 * (i+0.5) / ord - 1.0;

        for(error=1 ; error > 1.e-12; ) {

            error = 0;
            for( i=0; i<ord; i++) {  /* Update each point. */
                rooti = r[i];
                val = a[ord];
                p = a[ord];
                for(k=ord-1; k>= 0; k--) {
                    val = val * rooti + a[k];
                    if (k != i) p *= rooti - r[k];
                }
                delta = val/p;
                r[i] -= delta;
                error += delta*delta;
            }
        }

        /* Do a simple bubble sort to get them in the right order. */
        do {
            double tmplsp;
            swap = 0;
            for(i=0; i<ord-1;i++) {
                if(r[i] < r[i+1]) {
                    tmplsp = r[i];
                    r[i]=r[i+1];
                    r[i+1]=tmplsp;
                    swap++;
                }
            }
        } while (swap > 0);
    }
	
    /* The transformation as proposed by Wu and Chen. */
    static void kw(double[] r, int n) {
        double[] s = new double[100];
        double[] c = new double[100];
        int i, j, k;

        s[0] = 1.0;
        s[1] = -2.0;
        s[2] = 2.0;
        for(i=3;i<=n/2;i++) s[i] = s[i-2];

        for(k=0;k<=n;k++) {
            c[k] = r[k];
            j = 1;
            for(i=k+2;i<=n;i+=2) {
                c[k] += s[j]*r[i];        mpy++; add++;
                s[j] -= s[j-1];           add++;
                j++;                      ptr++;
            }
        }
        for(k=0;k<=n;k++) r[k] = c[k];
    }

    /**
     * This function converts LPC coefficients to LSP coefficients.
     * @param lpc      - LPC coefficients.
     * @param lpcrdr - order of LPC coefficients (10).
     * @param lsf   - LSP frequencies in the x domain.
     * @param nb     - number of sub-intervals (4).
     * @param d  - grid spacing interval (0.02).
     * @return the number of roots (the LSP coefs are returned in the array).
     */
    public static double[] lpc2lsp (final double[] lpc,
                               final int lpcrdr,
                               final double[] lsf,
                               final int nb,
                               final double d)
    {
      double psuml;
	  double psumr;
	  float temp_xr, xl, xr, xm=0;
	  double psumm;
      double temp_psumr;
      int i, j, m, flag, k;
      double[] Q;     // ptrs for memory allocation
      double[] P;
      int px;        // ptrs of respective P'(z) & Q'(z)
      int qx;
      int p;
      int q;
      double[] pt;    // ptr used for cheb_poly_eval() whether P' or Q'
      int roots = 0; // DR 8/2/94: number of roots found
      flag = 1;      // program is searching for a root when, 1 else has found one
      m = lpcrdr/2;  // order of P'(z) & Q'(z) polynomials

      /* Allocate memory space for polynomials */
      Q = new double[m+1];
      P = new double[m+1];

      /* determine P'(z)'s and Q'(z)'s coefficients where
      P'(z) = P(z)/(1 + z^(-1)) and Q'(z) = Q(z)/(1-z^(-1)) */

      px = 0;                      /* initialise ptrs       */
      qx = 0;
      p = px;
      q = qx;
      P[px++] = 1.0f;
      Q[qx++] = 1.0f;
      for (i=1; i<=m; i++){
        P[px++] = lpc[i]+lpc[lpcrdr+1-i]-P[p++];
        Q[qx++] = lpc[i]-lpc[lpcrdr+1-i]+Q[q++];
      }
      px = 0;
      qx = 0;
      for (i=0; i<m; i++){
        P[px] = 2*P[px];
        Q[qx] = 2*Q[qx];
        px++;
        qx++;
      }
      px = 0; /* re-initialise ptrs */
      qx = 0;

      /* Search for a zero in P'(z) polynomial first and then alternate to Q'(z).
      Keep alternating between the two polynomials as each zero is found  */

      xr = 0;    /* initialise xr to zero */
      xl = 1.0f; /* start at point xl = 1 */

      for (j=0; j<lpcrdr; j++){
        if (j%2 != 0) /* determines whether P' or Q' is eval. */
          pt = Q;
        else
          pt = P;

        psuml = cheb_poly_eva(pt, xl, lpcrdr); /* evals poly. at xl */
        flag = 1;
        while ((flag == 1) && (xr >= -1.0)) {
          float dd;
          /* Modified by JMV to provide smaller steps around x=+-1 */
          dd=(float)(d*(1-.9*xl*xl));
          if (Math.abs(psuml)<.2)
            dd *= .5;

          xr = xl - dd;                          /* interval spacing */
          psumr = cheb_poly_eva(pt, xr, lpcrdr); /* poly(xl-delta_x) */
          temp_psumr = psumr;
          temp_xr = xr;

          /* if no sign change increment xr and re-evaluate poly(xr). Repeat til
          sign change.
          if a sign change has occurred the interval is bisected and then
          checked again for a sign change which determines in which
          interval the zero lies in.
          If there is no sign change between poly(xm) and poly(xl) set interval
          between xm and xr else set interval between xl and xr and repeat till
          root is located within the specified limits */

          if ((psumr*psuml)<0.0) {
            roots++;

            psumm = psuml;
            for (k=0; k<=nb; k++){
              xm = (xl+xr)/2; /* bisect the interval */
              psumm = cheb_poly_eva(pt, xm, lpcrdr);
              if (psumm*psuml>0.) {
                psuml = psumm;
                xl = xm;
              }
              else {
                psumr = psumm;
                xr = xm;
              }
            }

            /* once zero is found, reset initial interval to xr */
            lsf[j] = xm;
            xl = xm;
            flag = 0; /* reset flag for next search */
          }
          else {
            psuml = temp_psumr;
            xl = temp_xr;
          }
        }
      }
      
      return lsf;
    }

    
    /**
     * This function evaluates a series of Chebyshev polynomials.
     * @param pt - coefficients of the polynomial to be evaluated.
     * @param x    - the point where polynomial is to be evaluated.
     * @param m    - order of the polynomial.
     * @return the value of the polynomial at point x.
     */
    public static final double cheb_poly_eva(final double[] pt,
                                            float x,
                                            final int m)
    {
      int i;
      double sum;
      float[] T;
      int m2 = m >> 1;
      /* Allocate memory for Chebyshev series formulation */
      T = new float[m2+1];
      /* Initialise values */
      T[0] = 1;
      T[1] = x;
      /* Evaluate Chebyshev series formulation using iterative approach  */
      /* Evaluate polynomial and return value also free memory space */
      sum = pt[m2] + pt[m2-1]*x;
      x *= 2;
      for (i=2; i<=m2; i++)
      {
        T[i] = x*T[i-1] - T[i-2];
        sum += pt[m2-i] * T[i];
      }
      return sum;
    }
}