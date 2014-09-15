#pragma version(1)
#pragma rs java_package_name(nz.ac.auckland.lablet.microphone);
//#pragma rs_fp_relaxed

uint32_t gWindowSize;
const float* gData;
float* gOutput;


static void hammingWindow(float* samples, uint32_t length) {
    for (int i = 0; i < length; i++)
        samples[i] *= (0.54f - 0.46f * cos(2 * M_PI * i / (length - 1)));
}


static void swap(float* array, int i, int j) {
    float temp = array[i];
    array[i] = array[j];
    array[j] = temp;
}

static void four1(float* data, int nn)
{
    int n, mmax, m, j, istep, i;
    float wtemp, wr, wpr, wpi, wi, theta;
    float tempr, tempi;

    // reverse-binary reindexing
    n = nn<<1;
    j=1;
    for (i=1; i<n; i+=2) {
        if (j>i) {
            swap(data, j-1, i-1);
            swap(data, j, i);
        }
        m = nn;
        while (m>=2 && j>m) {
            j -= m;
            m >>= 1;
        }
        j += m;
    };

    // here begins the Danielson-Lanczos section
    mmax=2;
    while (n>mmax) {
        istep = mmax<<1;
        theta = -(2*M_PI/mmax);
        wtemp = sin(0.5f*theta);
        wpr = -2.0*wtemp*wtemp;
        wpi = sin(theta);
        wr = 1.0;
        wi = 0.0;
        for (m=1; m < mmax; m += 2) {
            for (i=m; i <= n; i += istep) {
                j=i+mmax;
                tempr = wr*data[j-1] - wi*data[j];
                tempi = wr * data[j] + wi*data[j-1];

                data[j-1] = data[i-1] - tempr;
                data[j] = data[i] - tempi;
                data[i-1] += tempr;
                data[i] += tempi;
            }
            wtemp=wr;
            wr += wr*wpr - wi*wpi;
            wi += wi*wpr + wtemp*wpi;
        }
        mmax=istep;
    }
}


void root(const int *in, int *out, const void *usrData, uint32_t x, uint32_t y) {
    int start = in[0];

    int trafoLength = 2 * gWindowSize;
    float trafo[trafoLength];
    for (int i = 0; i < gWindowSize; i++) {
        trafo[2 * i] = gData[start + i];
        trafo[2 * i + 1] = 0;
    }
    //for (int i = gWindowSize; i < trafoLength; i++)
      //  trafo[i] = 0;
    hammingWindow(trafo, gWindowSize);

    four1(trafo, gWindowSize);

    for (int i = 1; i < trafoLength / 2; i += 2)
        gOutput[start + (i - 1) / 2] = sqrt(pow(trafo[i], 2) + pow(trafo[i - 1], 2));
}