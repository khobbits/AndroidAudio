package net.khhq.android.audio;

interface INStreamService
{
	void start(String url);
	String errors();
	void pause();
	void stop();
	int state();
}