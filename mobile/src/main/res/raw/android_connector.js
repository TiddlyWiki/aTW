/*\
title: $:/plugins/tiddlywiki/atw/modules/utils/android-connector.js
type: application/javascript
module-type: utils

android connector

\*/
(function(){

/*jslint node: true, browser: true */
/*global $tw: false */
"use strict";

	var AndroidConnector = function() {
		var self = this;
		this.palette = $tw.wiki.getTiddlerText("$:/palette");
		this.bgColor = $tw.utils.parseCSSColor($tw.wiki.extractTiddlerDataItem(this.palette,"page-background"));
		this.bgColorBefore = this.bgColor;
		$tw.wiki.addEventListener("change",function(changes) {
			self.updateSystemColors(changes);
			self.updateSiteTitles(changes);
		});
	};

	AndroidConnector.prototype.padTwo = function(s) {
		if(s.length === 1) {
			s = "0" + s;
		} else if(s.length === "0") {
			s = "00";
		}
		return s;
	};

	AndroidConnector.prototype.rgbaToHex = function(rgba) {
		if(rgba === null || rgba === undefined) {
			return ("#f4f4f4");
		}
		var r = parseInt(rgba[0], 10),
			g = parseInt(rgba[1], 10),
			b = parseInt(rgba[2], 10);
		var rString = this.padTwo(r.toString(16)),
			gString = this.padTwo(g.toString(16)),
			bString = this.padTwo(b.toString(16));
		return ("#" + rString + gString + bString);
	};

	AndroidConnector.prototype.updateSiteTitles = function(changes) {
		if($tw.utils.hop(changes,"$:/SiteTitle")) {
			window.twi.setSiteTitle('"' + $tw.wiki.getTiddlerText("$:/SiteTitle") + '"');
		} else if($tw.utils.hop(changes,"$:/SiteSubtitle")) {
			window.twi.setSiteSubtitle('"' + $tw.wiki.getTiddlerText("$:/SiteSubtitle") + '"');
		}
	};

	AndroidConnector.prototype.updateSystemColors = function(changes) {
		var bgColorTest,
			parsedColor,
			brightnessTarget,
			brightnessA,
			brightnessB,
			finalColor;
		if($tw.utils.hop(changes,"$:/palette")) {
			this.palette = $tw.wiki.getTiddlerText("$:/palette");
			bgColorTest = $tw.wiki.extractTiddlerDataItem(this.palette,"page-background");
			parsedColor = this.rgbaToHex($tw.utils.parseCSSColor(bgColorTest));
			if (parsedColor !== null && parsedColor !== undefined) {
				this.bgColor = parsedColor;
			} else {
				this.bgColor = bgColorTest;
			}
			if (this.bgColorBefore !== this.bgColor) {
				window.twi.updateSystemColors(this.bgColor);

				var rgbTarget = $tw.utils.parseCSSColor(this.bgColor),
					rgbColourA = $tw.utils.parseCSSColor($tw.wiki.extractTiddlerDataItem(this.palette,"foreground")),
					rgbColourB = $tw.utils.parseCSSColor("#ffffff");

				brightnessTarget = rgbTarget[0] * 0.299 + rgbTarget[1] * 0.587 + rgbTarget[2] * 0.114,
				brightnessA = rgbColourA[0] * 0.299 + rgbColourA[1] * 0.587 + rgbColourA[2] * 0.114,
				brightnessB = rgbColourB[0] * 0.299 + rgbColourB[1] * 0.587 + rgbColourB[2] * 0.114;

				finalColor = Math.abs(brightnessTarget - brightnessA) > Math.abs(brightnessTarget - brightnessB) ? this.rgbaToHex(rgbColourA) : "#ffffff";

				if(finalColor === "#ffffff") {
					window.twi.setDarkStatusbarFg();
				} else {
					window.twi.setLightStatusbarFg();
				}
				this.bgColorBefore = this.bgColor;
			}
		}
		if($tw.utils.hop(changes,this.palette)) {
			bgColorTest = $tw.wiki.extractTiddlerDataItem(this.palette,"page-background");
			var bgColorNew;
			parsedColor = this.rgbaToHex($tw.utils.parseCSSColor(bgColorTest));
			if (parsedColor !== null && parsedColor !== undefined) {
				bgColorNew = parsedColor;
			} else {
				bgColorNew = bgColorTest;
			}
			if(bgColorNew !== this.bgColor) {
				window.twi.updateSystemColors(bgColorNew);

				rgbTarget = $tw.utils.parseCSSColor(bgColorNew),
				rgbColourA = $tw.utils.parseCSSColor($tw.wiki.extractTiddlerDataItem(this.palette,"foreground")),
				rgbColourB = $tw.utils.parseCSSColor("#ffffff");

				brightnessTarget = rgbTarget[0] * 0.299 + rgbTarget[1] * 0.587 + rgbTarget[2] * 0.114,
				brightnessA = rgbColourA[0] * 0.299 + rgbColourA[1] * 0.587 + rgbColourA[2] * 0.114,
				brightnessB = rgbColourB[0] * 0.299 + rgbColourB[1] * 0.587 + rgbColourB[2] * 0.114;

				finalColor = Math.abs(brightnessTarget - brightnessA) > Math.abs(brightnessTarget - brightnessB) ? this.rgbaToHex(rgbColourA) : "#ffffff";

				if(finalColor === "#ffffff") {
					window.twi.setDarkStatusbarFg();
				} else {
					window.twi.setLightStatusbarFg();
				}
				this.bgColor = bgColorNew;
				this.bgColorBefore = bgColorNew;
			}
		}
	};

	AndroidConnector.prototype.getWikiColor = function(colorname) {
		var bgColorTest,
			parsedColor,
			result;
		bgColorTest = $tw.wiki.extractTiddlerDataItem(this.palette,colorname);
		parsedColor = this.rgbaToHex($tw.utils.parseCSSColor(bgColorTest));
		if (parsedColor !== null && parsedColor !== undefined) {
			result = parsedColor;
		} else {
			result = bgColorTest;
		}
		return result;
	};

	AndroidConnector.prototype.getWikiColorContrast = function(colorname,fallbackColor) {
		var bgColorTest,
			parsedColor,
			result;
		bgColorTest = $tw.wiki.extractTiddlerDataItem(this.palette,colorname);
		parsedColor = $tw.utils.parseCSSColor(bgColorTest);
		if (parsedColor !== null && parsedColor !== undefined) {
			result = parsedColor;
		} else {
			result = bgColorTest;
		}

		var rgbTarget = $tw.utils.parseCSSColor($tw.wiki.extractTiddlerDataItem(this.palette,"page-background")),
			rgbColourA = result,
			rgbColourB = $tw.utils.parseCSSColor(fallbackColor);

		var brightnessTarget = rgbTarget[0] * 0.299 + rgbTarget[1] * 0.587 + rgbTarget[2] * 0.114,
			brightnessA = rgbColourA[0] * 0.299 + rgbColourA[1] * 0.587 + rgbColourA[2] * 0.114,
			brightnessB = rgbColourB[0] * 0.299 + rgbColourB[1] * 0.587 + rgbColourB[2] * 0.114;

		var finalColor = Math.abs(brightnessTarget - brightnessA) > Math.abs(brightnessTarget - brightnessB) ? this.rgbaToHex(result) : fallbackColor;

		return finalColor;
	};

	return AndroidConnector;

})();
