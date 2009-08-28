package kjdss;

/**
 * @author Kris Fudalewski
 *
 * Fast Fourier Transformation class used for calculating the realtime spectrum analyzer in the
 * KJSpoceAndSpectrunAnalyzer component.
 */
public class KJFFT {

    private float[] xre;
    private float[] xim;
    private float[] mag;

    private float[] fftSin;
    private float[] fftCos;
    private int[]   fftBr;

    private int ss, ss2, nu;

	/**
	 * @param The amount of the sample provided to the "calculate" method to use during
	 *        FFT calculations, this is used to prepare the calculation tables in advance.
	 */
	public KJFFT( int pSampleSize ) {

        nu = (int)Math.ceil( Math.log( pSampleSize ) / Math.log( 2 ) );

		// -- Calculate the nearest sample size to a power of 2.
		ss = (int)Math.pow( 2, nu );

		ss2 = ss >> 1;

        // -- Allocate calculation buffers.
		xre = new float[ ss ];
        xim = new float[ ss ];
        mag = new float[ ss2 ];

        // -- Allocate FFT SIN/COS tables.
    	fftSin = new float[ nu * ss2 ];
    	fftCos = new float[ nu * ss2 ];

        prepareTables();

	}

    /**
     * @param  pSample The sample to compute FFT values on.
     * @return         The results of the calculation, normalized between 0.0 and 1.0.
     */
    private int bitrev( int pJ, int pNu ) {

        int j1 = pJ;
        int j2;
        int k = 0;

        for( int i = 0; i < pNu; i++ ) {
            j2 = j1 >> 1;
            k  = ( k << 1 ) + j1 - ( j2 << 1 );
            j1 = j2;
        }

        return k;

    }


    /**
     * Converts sound data over time into pressure values. (FFT)
     *
     * @param  pSample The sample to compute FFT values on.
     * @return         The results of the calculation, normalized between 0.0 and 1.0.
     */
    public float[] calculate( float[] pSample ) {

        int n2 = ss2;

        // -- Fill buffer.
        for ( int a = 0; a < pSample.length; a++ ) {
       		xre[ a ] = pSample[ a ];
        	xim[ a ] = 0.0f;
        }

        // -- Clear the remainder of the buffer.
        for ( int a = pSample.length; a < ss; a++ ) {
       		xre[ a ] = 0.0f;
        	xim[ a ] = 0.0f;
        }

        float tr, ti, c, s;
        int   k, kn2, x = 0;

        for ( int l = 0; l < nu; l++ ) {

            k = 0;

            while ( k < ss ) {

                for ( int i = 0; i < n2; i++ ) {

                	// -- Tabled sin/cos
                    c = fftCos[ x ];
                    s = fftSin[ x ];

                    kn2 = k + n2;

//                    System.out.println( "kn2: " + kn2 );

                    tr = xre[ kn2 ] * c + xim[ kn2 ] * s;
                    ti = xim[ kn2 ] * c - xre[ kn2 ] * s;

                    xre[ kn2 ] = xre[ k ] - tr;
                    xim[ kn2 ] = xim[ k ] - ti;
                    xre[ k ] += tr;
                    xim[ k ] += ti;

                    k++;
                    x++;

                }

                k += n2;

            }

            n2 >>= 1;

        }

        int r;

        // -- Reorder output.
        for( k = 0; k < ss; k++ ) {

        	// -- Use tabled BR values.
            r = fftBr[ k ];

            if ( r > k ) {

                tr = xre[ k ];

                xre[ k ] = xre[ r ];
                xre[ r ] = tr;

                ti = xim[ k ];

                xim[ k ] = xim[ r ];
                xim[ r ] = ti;

            }

        }

        // -- Calculate magnitude.
        for ( int i = 0; i < ss2; i++ ) {
            mag[ i ] = Math.abs( ( (float)( Math.sqrt( ( xre[ i ] * xre[ i ] ) + ( xim[ i ] * xim[ i ] ) ) ) / ss ) );
        }

        return mag;

    }

    private void prepareTables() {

        int n2 = ss2;
        int nu1 = nu - 1;

 //       System.out.println( "bs: " + ( nu * n2 ) );

        float p, arg;
        int   k = 0, x = 0;

        // -- Prepare SIN/COS tables.
        for ( int l = 0; l < nu; l++ ) {

//        	System.out.println( "*** l: " + l + " < " + nu );

            k = 0;

            while ( k < ss ) {

 //           	System.out.println( "    *** k: " + k + " < " + ss );

                for ( int i = 0; i < n2; i++ ) {

//                	System.out.println( "        " + k + " " + x );

                    p = bitrev( k >> nu1, nu );

                    arg = 2 * (float)Math.PI * p / ss;

                    fftSin[ x ] = (float)Math.sin( arg );
                    fftCos[ x ] = (float)Math.cos( arg );

                    k++;
                    x++;

                }

                k += n2;

            }

            nu1--;
            n2 >>= 1;

        }

        // -- Prepare bitrev table.
        fftBr = new int[ ss ];

        for( k = 0; k < ss; k++ ) {
            fftBr[ k ] = bitrev( k, nu );
        }

    }

}
