package ec.com.comohogar.inventario.validacion

import ec.com.comohogar.inventario.SesionAplicacion
import ec.com.comohogar.inventario.util.Constantes


class ValidacionBarra {

    companion object {
        fun validarEAN13Barra(barra : String): Boolean {
            val codeWithoutVd = barra.substring(0, 12)
            val regex = Regex(pattern = "[0-9]+")
            for (i in 0..barra.length-1) {
                if (!regex.containsMatchIn(input = barra[i].toString()) ) {
                    return  false
                }
            }


            val pretendVd = Integer.valueOf(barra.substring(12, 13))
            val e = sumEven(codeWithoutVd)
            val o = sumOdd(codeWithoutVd)
            val me = o * 3
            val s = me + e
            val dv = getEanVd(s)
            if (pretendVd != dv) {
                return  false
            }
            return true
        }


        fun validarCodigoInterno(barra : String): Boolean {
            val regex = "\\d{2,4}[-]\\d{1,4}".toRegex()
            return barra.matches(regex)
        }

        fun validarFormatoBarra(barra : String): Boolean {
           if(barra.contains("-")) {
               return true
           }else if(barra.length != 13){
               return false
            }else{
                val regex = "\\d{13}".toRegex()
                return barra.matches(regex)
            }
        }

        private fun sumEven(code: String): Int {
            var sum = 0
            for (i in 0 until code.length) {
                if (isEven(i)) {
                    sum += Character.getNumericValue(code[i])
                }
            }
            return sum
        }


        private fun sumOdd(code: String): Int {
            var sum = 0
            for (i in 0 until code.length) {
                if (!isEven(i)) {
                    sum += Character.getNumericValue(code[i])
                }
            }
            return sum
        }

        private fun isEven(i: Int): Boolean {
            return i % 2 == 0
        }

        private fun getEanVd(s: Int): Int {
            if(10 - s % 10 == 10){
                return 0
            }
            return 10 - s % 10
        }

    }
}