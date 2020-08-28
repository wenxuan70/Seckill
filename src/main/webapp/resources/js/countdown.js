(function($){function Countdown(){this._nextId=0;this._inst=[];this.regional=[];this.regional[""]={labels:["Days","Hours","Minutes","Seconds"],compactDays:"d",timeSeparator:":"};this._defaults={showDays:"asNeeded",showSeconds:true,compact:false,description:"",onExpiry:null,onTick:null};$.extend(this._defaults,this.regional[""])}$.extend(Countdown.prototype,{markerClassName:"hasCountdown",_register:function(inst){var id=this._nextId++;this._inst[id]=inst;return id},_getInst:function(id){return this._inst[id]||id},setDefaults:function(settings){extendRemove(this._defaults,settings||{})},_attachCountdown:function(target,inst){target=$(target);if(target.is("."+this.markerClassName)){return}target.addClass(this.markerClassName);target[0]._cdnId=inst._id;inst._target=target;this._updateCountdown(inst._id)},_updateCountdown:function(id){var inst=this._getInst(id);inst._target.html(inst._generateHTML());var onTick=inst._get("onTick");if(onTick){onTick.apply(inst._target[0],[inst._periods])}if(inst._now.getTime()>=inst._getUntil(inst._now).getTime()){if(inst._timer){var onExpiry=inst._get("onExpiry");if(onExpiry){onExpiry.apply(inst._target[0],[])}}inst._timer=null}else{inst._timer=setTimeout("$.countdown._updateCountdown("+inst._id+")",(inst._get("showSeconds")?1:30)*980)}},_removeCountdown:function(target){target=$(target);if(!target.is("."+this.markerClassName)){return}target.removeClass(this.markerClassName);target.empty();clearTimeout(this._inst[target[0]._cdnId]._timer);this._inst[target[0]._cdnId]=null;target[0]._cdnId=undefined}});function CountdownInstance(settings){this._id=$.countdown._register(this);this._target=null;this._timer=null;this._now=null;this._periods=[0,0,0,0];this._settings=extendRemove({},settings||{})}$.extend(CountdownInstance.prototype,{_get:function(name){return this._settings[name]!=null?this._settings[name]:$.countdown._defaults[name]},_generateHTML:function(){var showDays=this._get("showDays");var showSeconds=this._get("showSeconds");this._now=new Date;this._now.setMilliseconds(0);var until=this._getUntil(this._now);if(this._now.getTime()>until.getTime()){this._now=until}var diff=Math.floor((until.getTime()-this._now.getTime())/1e3);this._periods[0]=Math.floor(diff/86400);this._periods[1]=Math.floor(diff/3600)-this._periods[0]*24;this._periods[2]=Math.floor(diff/60)-(this._periods[0]*1440+this._periods[1]*60);this._periods[3]=!showSeconds?0:diff-(this._periods[0]*86400+this._periods[1]*3600+this._periods[2]*60);showDays=showDays=="always"||showDays=="asNeeded"&&this._periods[0]>0;if(!showDays){this._periods[1]+=this._periods[0]*24;this._periods[0]=0}var labels=this._get("labels");var compact=this._get("compact");var compactDays=this._get("compactDays");var timeSeparator=this._get("timeSeparator");var description=this._get("description")||"";var twoDigits=function(value){return(value<10?"0":"")+value};var html=(compact?'<div class="countdown_row countdown_amount">'+(showDays?this._periods[0]+compactDays+" ":"")+twoDigits(this._periods[1])+timeSeparator+twoDigits(this._periods[2])+(showSeconds?timeSeparator+twoDigits(this._periods[3]):""):'<div class="countdown_row countdown_show'+(2+(showDays?1:0)+(showSeconds?1:0))+'">'+(showDays?'<div class="countdown_section"><span class="countdown_amount">'+this._periods[0]+"</span><br/>"+labels[0]+"</div>":"")+'<div class="countdown_section"><span class="countdown_amount">'+(this._periods[1]+(showDays?0:this._periods[0]*24))+"</span><br/>"+labels[1]+"</div>"+'<div class="countdown_section"><span class="countdown_amount">'+this._periods[2]+"</span><br/>"+labels[2]+"</div>"+(showSeconds?'<div class="countdown_section"><span class="countdown_amount">'+this._periods[3]+"</span><br/>"+labels[3]+"</div>":""))+"</div>"+(description?'<div class="countdown_row countdown_descr">'+description+"</div>":"");return html},_getUntil:function(now){var until=this._get("until")||now;until.setMilliseconds(0);return until}});function extendRemove(target,props){$.extend(target,props);for(var name in props){if(props[name]==null){target[name]=null}}return target}$.fn.attachCountdown=function(settings){return this.each(function(){$.countdown._attachCountdown(this,new CountdownInstance(settings))})};$.fn.removeCountdown=function(){return this.each(function(){$.countdown._removeCountdown(this)})};$.fn.changeCountdown=function(settings){return this.each(function(){var inst=$.countdown._getInst(this._cdnId);if(inst){extendRemove(inst._settings,settings||{});$.countdown._updateCountdown(inst._id)}})};$(document).ready(function(){$.countdown=new Countdown})})(jQuery);