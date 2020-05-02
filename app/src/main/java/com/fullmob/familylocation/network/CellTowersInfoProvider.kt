package com.fullmob.familylocation.network

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.*
import android.telephony.TelephonyManager.*
import androidx.core.app.ActivityCompat
import org.json.JSONArray
import org.json.JSONObject


class CellTowerInfo(
    val radio: String = "",
    val isRegistered: Boolean,
    val properties: MutableMap<String, Any> = HashMap()
) {
    companion object {
        const val ALPHA_LONG = "alpha_long"
        const val ALPHA_SHORT = "alpha_short"
        const val MCC = "mcc"
        const val MNC = "mnc"
        const val SIGNAL = "signal"
        const val RADIO = "radio"
        const val ASU = "asu"
        const val TA = "ta"

        const val LTE_LAC = "lac"
        const val LTE_CID = "cid"
        const val LTE_PSC = "psc"

        const val WCDMA_LAC = LTE_LAC
        const val WCDMA_CID = LTE_CID
        const val WCDMA_PSC = LTE_PSC

        const val TDSCDMA_LAC = LTE_LAC
        const val TDSCDMA_CID = LTE_CID

        const val GSM_LAC = LTE_LAC
        const val GSM_CID = LTE_CID

        const val CDMA_LAC = LTE_LAC
        const val CDMA_CID = LTE_CID
    }
}

class CellTowersInfo(
    var radioType: String = "",
    var mcc: String = "",
    var mnc: String = "",
    var cells: MutableList<CellTowerInfo> = ArrayList()
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json["address"] = 1
        json["token"] = "f9d1bc6198ddce"
        json["radio"] = radioType
        json["mnc"] = mnc
        json["mcc"] = mcc
        json["cells"] = JSONArray()
        cells.forEach { cell ->
            val jsonCell = JSONObject()
            cell.properties.entries.forEach { (key, value) ->
                jsonCell[key] = value
            }
            jsonCell[CellTowerInfo.RADIO] = cell.radio
            json.getJSONArray("cells").put(jsonCell)
        }
        return json
    }
}

private operator fun JSONObject.set(name: String, value: Any) {
    this.put(name, value)
}

class CellTowersInfoProvider(private val ctx: Context) {


    @SuppressLint("MissingPermission")
    fun getCellTowerIds(): CellTowersInfo {
        val cellTowers = CellTowersInfo()
        if (isPermissionGranted()) return cellTowers
        val tel = ctx.getSystemService(TELEPHONY_SERVICE) as TelephonyManager?
        cellTowers.radioType = when (tel?.phoneType ?: 0) {
            PHONE_TYPE_GSM -> "gsm"
            PHONE_TYPE_CDMA -> "cdma"
            PHONE_TYPE_SIP -> "sip"
            else -> ""
        }
        val cells = tel?.allCellInfo
        cells?.forEach { cell ->
            when (cell) {
                is CellInfoLte -> cellInfoLte(cell)?.let { cellTowers.cells.add(it) }
                is CellInfoGsm -> cellInfoGsm(cell)?.let { cellTowers.cells.add(it) }
                is CellInfoCdma -> cellInfoCdma(cell)?.let { cellTowers.cells.add(it) }
                is CellInfoWcdma -> cellInfoWcdma(cell)?.let { cellTowers.cells.add(it) }
                is CellInfoTdscdma -> cellInfoTdscdma(cell)?.let { cellTowers.cells.add(it) }
                is CellInfoNr -> createCellInfoNr(cell)?.let { cellTowers.cells.add(it) }
            }
        }
        cellTowers.cells.forEach { cell ->
            if (cell.isRegistered) {
                cell.properties[CellTowerInfo.MCC]?.let { cellTowers.mcc = it.toString() }
                cell.properties[CellTowerInfo.MNC]?.let { cellTowers.mnc = it.toString() }
                cellTowers.radioType = cell.radio
            }
        }

        return cellTowers
    }

    private fun cellInfoLte(cellInfo: CellInfoLte): CellTowerInfo? {
        val cellTower = CellTowerInfo(radio = "lte", isRegistered = cellInfo.isRegistered)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            cellTower.properties[CellTowerInfo.MCC] = cellInfo.cellIdentity.mccString ?: ""
            cellTower.properties[CellTowerInfo.MNC] = cellInfo.cellIdentity.mncString ?: ""
        } else {
            cellTower.properties[CellTowerInfo.MCC] = cellInfo.cellIdentity.mcc
            cellTower.properties[CellTowerInfo.MNC] = cellInfo.cellIdentity.mnc
        }
        cellTower.properties[CellTowerInfo.LTE_CID] = cellInfo.cellIdentity.ci
        cellTower.properties[CellTowerInfo.LTE_LAC] = cellInfo.cellIdentity.tac
        if (cellInfo.cellIdentity.pci != Int.MAX_VALUE) {
            cellTower.properties[CellTowerInfo.LTE_PSC] = cellInfo.cellIdentity.pci
        }

        cellTower.properties[CellTowerInfo.SIGNAL] = cellInfo.cellSignalStrength.dbm
        cellTower.properties[CellTowerInfo.ASU] = cellInfo.cellSignalStrength.asuLevel
        cellTower.properties[CellTowerInfo.TA] = cellInfo.cellSignalStrength.timingAdvance

        return if (cellInfo.cellIdentity.ci == 0) {
            null
        } else cellTower
    }

    private fun cellInfoGsm(cellInfo: CellInfoGsm): CellTowerInfo? {
        val cellTower = CellTowerInfo(radio = "gsm", isRegistered = cellInfo.isRegistered)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            cellTower.properties[CellTowerInfo.MCC] = cellInfo.cellIdentity.mccString ?: ""
            cellTower.properties[CellTowerInfo.MNC] = cellInfo.cellIdentity.mncString ?: ""
        } else {
            cellTower.properties[CellTowerInfo.MCC] = cellInfo.cellIdentity.mcc
            cellTower.properties[CellTowerInfo.MNC] = cellInfo.cellIdentity.mnc
        }
        if (cellInfo.cellIdentity.cid == 0) return null
        cellTower.properties[CellTowerInfo.GSM_CID] = cellInfo.cellIdentity.cid
        if (cellInfo.cellIdentity.lac in 0..65533) {
            cellTower.properties[CellTowerInfo.GSM_LAC] = cellInfo.cellIdentity.lac
        }
        cellTower.properties[CellTowerInfo.ASU] = cellInfo.cellSignalStrength.asuLevel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            cellTower.properties[CellTowerInfo.TA] = cellInfo.cellSignalStrength.timingAdvance
        }
        cellTower.properties[CellTowerInfo.SIGNAL] = cellInfo.cellSignalStrength.dbm

        return cellTower
    }

    private fun cellInfoWcdma(cellInfo: CellInfoWcdma): CellTowerInfo? {
        val cellTower = CellTowerInfo(radio = "umts", isRegistered = cellInfo.isRegistered)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            cellTower.properties[CellTowerInfo.MCC] = cellInfo.cellIdentity.mccString ?: ""
            cellTower.properties[CellTowerInfo.MNC] = cellInfo.cellIdentity.mncString ?: ""
        } else {
            cellTower.properties[CellTowerInfo.MCC] = cellInfo.cellIdentity.mcc
            cellTower.properties[CellTowerInfo.MNC] = cellInfo.cellIdentity.mnc
        }
        cellTower.properties[CellTowerInfo.WCDMA_CID] = cellInfo.cellIdentity.cid
        cellTower.properties[CellTowerInfo.ASU] = cellInfo.cellSignalStrength.asuLevel
        cellTower.properties[CellTowerInfo.WCDMA_LAC] = cellInfo.cellIdentity.lac
        cellTower.properties[CellTowerInfo.WCDMA_PSC] = cellInfo.cellIdentity.psc
        cellTower.properties[CellTowerInfo.SIGNAL] = cellInfo.cellSignalStrength.dbm

        return cellTower
    }

    private fun cellInfoCdma(cellInfo: CellInfoCdma): CellTowerInfo? {
        val cellTower = CellTowerInfo(radio = "cdma", isRegistered = cellInfo.isRegistered)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            cellTower.properties[CellTowerInfo.MCC] = cellInfo.cellIdentity.networkId
        } else {
            cellTower.properties[CellTowerInfo.MCC] = cellInfo.cellIdentity.networkId
        }
        cellTower.properties[CellTowerInfo.MNC] = cellInfo.cellIdentity.systemId
        cellTower.properties[CellTowerInfo.CDMA_CID] = cellInfo.cellIdentity.basestationId
        cellTower.properties[CellTowerInfo.CDMA_LAC] = cellInfo.cellIdentity.networkId
        cellTower.properties[CellTowerInfo.ASU] = cellInfo.cellSignalStrength.asuLevel
        cellTower.properties[CellTowerInfo.SIGNAL] = cellInfo.cellSignalStrength.dbm


        return cellTower
    }

    private fun createCellInfoNr(cellInfo: CellInfoNr): CellTowerInfo? {
        val cellTower = CellTowerInfo(radio = "nr", isRegistered = cellInfo.isRegistered)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            cellTower.properties[CellTowerInfo.ALPHA_LONG] =
                cellInfo.cellIdentity.operatorAlphaLong ?: ""
            cellTower.properties[CellTowerInfo.ALPHA_SHORT] =
                cellInfo.cellIdentity.operatorAlphaShort ?: ""
        }


        return cellTower
    }

    private fun cellInfoTdscdma(cellInfo: CellInfoTdscdma): CellTowerInfo? {
        val cellTower = CellTowerInfo(radio = "tdscdma", isRegistered = cellInfo.isRegistered)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            cellTower.properties[CellTowerInfo.MCC] = cellInfo.cellIdentity.mccString ?: ""
            cellTower.properties[CellTowerInfo.MNC] = cellInfo.cellIdentity.mncString ?: ""
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            cellTower.properties[CellTowerInfo.TDSCDMA_CID] = cellInfo.cellIdentity.cid
            cellTower.properties[CellTowerInfo.TDSCDMA_LAC] = cellInfo.cellIdentity.lac
        }

        return cellTower
    }


    private fun isPermissionGranted(): Boolean {
        val granted =
            ActivityCompat.checkSelfPermission(ctx, "android.permission.READ_PHONE_STATE")
        if (granted != PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }
}
