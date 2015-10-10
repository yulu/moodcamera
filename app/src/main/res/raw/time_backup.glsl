precision mediump float; 
uniform sampler2D u_Texture0;
uniform sampler2D u_Texture1;
varying vec2 v_TexCoordinate; 

vec3 brightness(vec3 color, float brightness) {
	float scaled = brightness / 2.0;
	if (scaled < 0.0) {
		return color * (1.0 + scaled);
	} else {
		return color + ((1.0 - color) * scaled);
	}
}

vec3 contrast(vec3 color, float contrast) {
	const float PI = 3.14159265;
	return min(vec3(1.0), ((color - 0.5) * (tan((contrast + 1.0) * PI / 4.0) ) + 0.5));
}

vec3 overlay(vec3 overlayComponent, vec3 underlayComponent, float alpha) {
	vec3 underlay = underlayComponent * alpha;
	return underlay * (underlay + (2.0 * overlayComponent * (1.0 - underlay)));
}

vec3 multiplyWithAlpha(vec3 overlayComponent, float alpha, vec3 underlayComponent) {
	return underlayComponent * overlayComponent * alpha;
}

vec3 screenPixelComponent(vec3 maskPixelComponent, float alpha, vec3 imagePixelComponent) {
	return 1.0 - (1.0 - (maskPixelComponent * alpha)) * (1.0 - imagePixelComponent);
}

vec3 convertRGB2HSV(vec3 rgbcolor){
	float h, s, v;
	
	float r = rgbcolor.r;
	float g = rgbcolor.g;
	float b = rgbcolor.b;
	v = max(r, max(g, b));
	float maxval = v;
	float minval = min(r, min(g,b));
	
	if(maxval == 0.)
		s = 0.0;
	else
		s = (maxval - minval)/maxval;
		
	if(s == 0.)
		h = 0.; 
	else{
		float delta = maxval - minval;
		
		if(r == maxval)
			h = (g-b)/delta;
		else
			if(g == maxval)
				h = 2.0 + (b-r)/delta;
			else
				if(b == maxval)
					h = 4.0 + (r-g)/delta;
		
		h*= 60.;
		if( h < 0.0)
			h += 360.;	
	}
	
	return vec3(h, s, v);
}

vec3 rgbToHsv(vec3 color) {
	vec3 hsv;
	
	float mmin = min(color.r, min(color.g, color.b));
	float mmax = max(color.r, max(color.g, color.b));
	float delta = mmax - mmin;
	
	hsv.z = mmax;
	hsv.y = delta / mmax;

	if (color.r == mmax) {
		hsv.x = (color.g - color.b) / delta;
	} else if (color.g == mmax) {
		hsv.x = 2.0 + (color.b - color.r) / delta;
	} else {
		hsv.x = 4.0 + (color.r - color.g) / delta;
	}
	
	hsv.x *= 0.166667;
	if (hsv.x < 0.0) {
		hsv.x += 1.0;
	}
	
	return hsv;
}

vec3 hsvToRgb(vec3 hsv) {
	if (hsv.y == 0.0) {
		return vec3(hsv.z);
	} else {
		float i;
		float aa, bb, cc, f;

		float h = hsv.x;
		float s = hsv.y;
		float b = hsv.z;

		if (h == 1.0) {
			h = 0.0;
		}

		h *= 6.0;
		i = floor(h);
		f = h - i;
		aa = b * (1.0 - s);
		bb = b * (1.0 - (s * f));
		cc = b * (1.0 - (s * (1.0 - f)));
		
		if (i == 0.0) return vec3(b, cc, aa);
		if (i == 1.0) return vec3(bb, b, aa);
		if (i == 2.0) return vec3(aa, b, cc);
		if (i == 3.0) return vec3(aa, bb, b);
		if (i == 4.0) return vec3(cc, aa, b);
		if (i == 5.0) return vec3(b, aa, bb);
	}
}

vec3 saturation(vec3 color, float sat) {
	const float lumaR = 0.212671;
	const float lumaG = 0.715160;
	const float lumaB = 0.072169;
	
	float v = sat + 1.0;
	float i = 1.0 - v;
	float r = i * lumaR;
	float g = i * lumaG;
	float b = i * lumaB;
	
	mat3 mat = mat3(r + v, r, r, g, g + v, g, b, b, b + v);
	
	return mat * color;
}

vec3 softglow(vec3 color) {
    vec3 val = saturation(color, -0.8);
    
    val = 1./(1. + exp(-1.*(2.+0.85*20.)*(val - 0.5)));
    val = val * 1.2;
    val = clamp(val, vec3(0.), vec3(1.));
    
    return val;
} 

void main() 
{ 
	 /*vec4 baseColor;
	 vec4 filterColor;
	 
	 baseColor = texture2D(u_Texture0, v_TexCoordinate);
	 filterColor = texture2D(u_Texture1, v_TexCoordinate);
	 
     gl_FragColor = baseColor*(filterColor+0.25); */
     
    float dx = 1./720.;
	float dy = 1./720.;
	const vec3 W = vec3(0.2125, 0.7154, 0.0721);
	
	vec3 color = texture2D(u_Texture0, v_TexCoordinate).rgb;
	vec3 frgb = texture2D(u_Texture1, v_TexCoordinate).rgb;

	color.r = color.r * 0.843 + 0.157;
	color.b = color.b * 0.882 + 0.118;
	
	vec3 hsv = rgbToHsv(color);
	hsv.y = hsv.y * 0.35;
	color = hsvToRgb(hsv);
	
	color = saturation(color, 0.65);
	color *= vec3(1.0, 0.891, 0.733);

	
	//texture
	color = multiplyWithAlpha(frgb, 1.5, color);	
	
	
	gl_FragColor = vec4(color, 1.0);
}