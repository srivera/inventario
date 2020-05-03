package ec.com.comohogar.inventario.scanner;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.ScannerConfig;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerInfo;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.StatusData;

import java.util.List;

import ec.com.comohogar.inventario.R;
import ec.com.comohogar.inventario.validacion.ValidacionBarra;

public abstract class ScanActivity extends AppCompatActivity implements
        EMDKManager.EMDKListener, Scanner.DataListener, Scanner.StatusListener,
        BarcodeManager.ScannerConnectionListener {

    private Snackbar snackBar;

    //Zebra SDK objects
    private EMDKManager emdkManager;
    private BarcodeManager barcodeManager;
    private Scanner scanner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //start EMDK service
        askForEMDKConnection();
    }

    @Override
    protected void onPause() {
        // De-initialize scanner
        deInitScanner();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // De-initialize scanner
        deInitScanner();

        super.onDestroy();
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
        this.emdkManager = emdkManager;
        // Add connection listener
        attachListenersAndInitScanner();
    }


    @Override
    public void onClosed() {
        if (emdkManager != null) {
            // Remove connection listener
            if (barcodeManager != null){
                barcodeManager.removeConnectionListener(this);
                barcodeManager = null;
            }
            // Release all the resources
            emdkManager.release();
            emdkManager = null;
        }
    }

    @Override
    public void onStatus(StatusData statusData) {
        StatusData.ScannerStates state = statusData.getState();

        String estado = "";
        switch(state) {
            case IDLE:
                try {
                    // An attempt to use the scanner continuously and rapidly (with a delay < 100 ms between scans)
                    // may cause the scanner to pause momentarily before resuming the scanning.
                    // Hence add some delay (>= 100ms) before submitting the next read.
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (scanner != null && !scanner.isReadPending()) {
                        scanner.read();
                    }
                } catch (ScannerException e) {
                    e.printStackTrace();
                    //we have a problem, show the user the snackbar for trying to solve it
                    showEMDKConnectionError();
                }
                estado = "Listo para escanear..";
                break;
            case SCANNING:
                hideSnackBar();
                estado = "Escaneando..";
                break;
            case WAITING:
                estado = "Listo para escanear..";
                break;
            case DISABLED:
                estado = "El scanner no esta habilitado";
                break;
            default:
                break;
            case ERROR:
                estado = "Error";
                break;
        }
        refrescarEstado(estado);
    }

    @Override
    public void onData(ScanDataCollection scanDataCollection) {
        if (scanDataCollection != null && scanDataCollection.getResult() == ScannerResults.SUCCESS) {
            List<ScanDataCollection.ScanData> scanData = scanDataCollection.getScanData();
            if (scanData.size() > 0) {
                //get only the first result
                ScanDataCollection.ScanData data = scanData.get(0);
                String qrCodeData = data.getData();

                //we need to use the runOnUiThread to be able to read this
                runOnUiThread(() -> {
                    onQRCodeReaded(qrCodeData);
                });
            }
        }
    }

    @Override
    public void onConnectionChange(ScannerInfo scannerInfo,
                                   BarcodeManager.ConnectionState connectionState) {
        //we don't need to performs logic here currently
        Log.d("connection changed", connectionState.name());
    }

    /**
     * Get an instance of the barcode reader and start listening to scan events
     */
    private void attachListenersAndInitScanner(){
        // Acquire the barcode manager resources
        barcodeManager = (BarcodeManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);
        if (barcodeManager != null) {
            barcodeManager.addConnectionListener(this);
            // Initialize scanner
            initScanner();
        }
    }

    /**
     * Destroy all the resources associated with the current EMDK service
     * Must be called before closing the app or leaving the current Activity
     */
    private void deInitScanner() {
        if (scanner != null) {
            //here we are separating the various try/catch because they are grouped by scope
            //if one of them crash it doesn't mean the others cannot be performed
            try {
                scanner.cancelRead();
                scanner.disable();
            } catch (Exception ignored) {}
            try {
                scanner.removeDataListener(this);
                scanner.removeStatusListener(this);
            } catch (Exception ignored) {}
            try {
                scanner.release();
            } catch (Exception ignored) {}
            scanner = null;
        }

        if (barcodeManager != null) {
            barcodeManager.removeConnectionListener(this);
            barcodeManager = null;
        }

        // Release the barcode manager resources
        if (emdkManager != null) {
            //we need to release all the resources as described here
            //https://developer.zebra.com/thread/34378
            emdkManager.release(EMDKManager.FEATURE_TYPE.BARCODE);
            emdkManager.release();
            emdkManager = null;
        }
    }

    /**
     * Init scanner object and start reading for scans
     */
    private void initScanner() {
        if (barcodeManager != null && scanner == null) {
            //get the default scanner for the barcode/qrcode manager, this should already
            //be the optimized one
            scanner = barcodeManager.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT);
            if (scanner != null) {
                //attach listener and set config
                scanner.addDataListener(this);
                scanner.addStatusListener(this);
                scanner.triggerType = Scanner.TriggerType.HARD;
                try {
                    scanner.enable();
                    ScannerConfig config = scanner.getConfig();
                    config.readerParams.readerSpecific.imagerSpecific.beamTimer = 1000;
                    scanner.setConfig(config);
                    hideSnackBar();
                } catch (ScannerException e) {
                    e.printStackTrace();
                }
            } else {
                //if the scanner is still not initialized here we sure have a problem
                showEMDKConnectionError();
            }
        } else if (barcodeManager == null) {
            //if the barcodeManager is still not initialized here we sure have a problem
            showEMDKConnectionError();
        }
    }

    /**
     * start the EMDK service connection
     */
    private void askForEMDKConnection() {
        EMDKManager.getEMDKManager(getApplicationContext(), this);
    }

    /**
     * show snackbar connnection error
     */
    private void showEMDKConnectionError() {
        snackBar = Snackbar
                .make(findViewById(android.R.id.content), "can't connect to the Zebra scanner", Snackbar.LENGTH_INDEFINITE)
                .setAction("retry", v -> {
                    deInitScanner();
                    askForEMDKConnection();
                });
        snackBar.show();
    }

    /**
     * Hide the snackbar
     */
    private void hideSnackBar() {
        if (snackBar != null) {
            snackBar.dismiss();
        }
    }

    /**
     * This method can be implemented by every activity which want to listen to the
     * scanner reads
     */
    protected void onQRCodeReaded(String data) {
        if(data.length() == 12) {
            data ="0" + data;
        }else if(data.contains("-") && data.startsWith("00")){
            data = data.substring(2, data.length());
        }
        if(data.length() == 13) {
            if (ValidacionBarra.Companion.validarEAN13Barra(data)) {
                refrescarPantalla(data);
            } else {
                refrescarPantalla(getString(R.string.formato_incorrecto));
            }
        }else{
            refrescarPantalla(data);
        }
    }

    public void refrescarPantalla(String codigoLeido) {
        Log.i("padre", "padre");
    }

    public void refrescarEstado(String result) {
    }
}