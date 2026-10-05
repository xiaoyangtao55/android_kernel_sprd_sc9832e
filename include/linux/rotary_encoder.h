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
	/*
	 * Software contact debounce time in milliseconds. Mechanical
	 * encoders bounce for a few hundred microseconds on every edge,
	 * and this driver would otherwise decode those bounces as extra
	 * detents. Zero disables debouncing.
	 *
	 * This is done in software rather than through the GPIO
	 * controller because the Spreadtrum "sprd-ap-gpio" controller
	 * used by these boards has no working set_debounce.
	 */
	unsigned int debounce_ms;
};

#endif /* __ROTARY_ENCODER_H__ */
