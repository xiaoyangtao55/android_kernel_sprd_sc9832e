#ifndef __ROTARY_ENCODER_H__
#define __ROTARY_ENCODER_H__

struct rotary_encoder_platform_data {
	unsigned int steps;
	unsigned int axis;
	unsigned int gpio_a;
	unsigned int gpio_b;
	unsigned int inverted_a;
	unsigned int inverted_b;
	unsigned int steps_per_period;
	bool relative_axis;
	bool rollover;
	bool wakeup_source;
	/*
	 * Key-emulation mode: when key_event is set, each detent is reported as
	 * a key press instead of REL/ABS motion, so the knob works with the
	 * stock Android input stack (volume keys need no app-side support).
	 */
	bool key_event;
	unsigned int key_code_cw;
	unsigned int key_code_ccw;
};

#endif /* __ROTARY_ENCODER_H__ */
