package com.eugenisb.alphatest.clases

import java.io.Serializable

class Opinion (val creator: String, val moviePoster: String, val opinionComment: String,
               val public: Boolean, val rating: Int) : Serializable {
    constructor() : this("","","",false,0)


}