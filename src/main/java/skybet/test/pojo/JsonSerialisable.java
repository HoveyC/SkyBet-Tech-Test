/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skybet.test.pojo;

import io.vertx.core.json.Json;

/**
 *
 * This class is extened by all the json POJO's to provide a readable
 * way to serialising the POJO.
 * 
 * @author chrishovey
 *
 */
public abstract class JsonSerialisable {
    
    public String toJsonString() {
        return Json.encode(this);
    }
}
