import java.util.Date;


public class Temp {
	
    /* Operations counters. Not needed in a real application. */
    static int mpy=0;
    static int add=0;
    static int ptr=0;
	
	public static void main(String args[]) {
		
		double[] lpc = {1.0000, -0.6433, -0.1737, 0.1360, 0.0559, -0.0467, -0.0728, 0.0400, -0.0573, -0.0414, -0.1180};

		
//		double[] lsf = lpc2lsf(lpc, 1);
//		
//		for(int i = 0; i < lsf.length; i++) {
//			System.out.print(lsf[i] + " ");
//		}
		
		long start = System.currentTimeMillis(); 
		
		double[] lsf = new double[lpc.length - 1];
		
		lsf = lpc2lsp(lpc, 10, lsf, 4, 0.02);
		
		for(int i = 0; i < lsf.length; i++) {
			System.out.print(lsf[i] + " ");
		}
		
		long runningTime =  System.currentTimeMillis() - start;
		System.out.println("Runtime: " + runningTime + "ms");
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
