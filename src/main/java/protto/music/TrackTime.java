package protto.music;

import org.apache.commons.lang3.math.NumberUtils;

public class TrackTime {
	
	public long ms   = 0;
	public int secs  = -1;
	public int mins  = -1;
	public int hours = -1;
	private transient String buffer;
	
	public TrackTime() {
		setValues(ms);
	}

	public TrackTime(long ms) {
		setValues(ms);
	}
	
	public TrackTime(String time) {
		try {
			if (NumberUtils.isCreatable(time)) {
				setValues(Long.parseLong(time) * 1000L);
			} else {
				buffer = "";
				for (char chr : time.toCharArray()) {
					chr = Character.toLowerCase(chr);
					if (chr == 'h' || chr == 'm' || chr == 's') {
						if (chr == 'h')
							hours = Integer.parseInt(buffer);
						else if (chr == 'm')
							mins  = Integer.parseInt(buffer);
						else if (chr == 's')
							secs  = Integer.parseInt(buffer);
						buffer = "";
					} else
						buffer += chr;
				}
				if (buffer != "" && NumberUtils.isCreatable(buffer))
					secs = (secs > -1 ? secs : 0) + Integer.parseInt(buffer);
				if (hours > -1 || mins > -1 || secs > -1) {
					ms = 0;
					if (hours > -1) ms += hours * 60 * 60 * 1000;
					if (mins > -1)  ms += mins * 60 * 1000;
					if (secs > -1)  ms += secs * 1000;
				}
				setValues(ms);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			setValues(0);
		}
	}
	
	public TrackTime add(TrackTime time) {
		return new TrackTime((ms + time.ms >= 0) ? ms + time.ms : 0);
	}
	
	public TrackTime sub(TrackTime time) {
		return new TrackTime((ms - time.ms >= 0) ? ms - time.ms : 0);
	}
	
	private void setValues(long ms) {
		this.ms = ms;
		secs = (int)(ms / 1000) % 60;
		mins = (int)(ms / (1000 * 60)) % 60;
		hours = (int)(ms / (1000 * 60 * 60)) % 24;
	}
	
	@Override
	public String toString() {
		if (hours < 1)
			return String.format("%02d:%02d", mins, secs);
		return String.format("%02d:%02d:%02d", hours, mins, secs);
	}
}
