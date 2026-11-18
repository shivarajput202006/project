import { Link } from "react-router-dom";

export default function PrimaryButton({
  children,
  to,
  href,
  variant = "primary",
  className = "",
  ...props
}) {
  const classes = `button button-${variant} ${className}`.trim();

  if (to) {
    return <Link className={classes} to={to} {...props}>{children}</Link>;
  }

  return <a className={classes} href={href} {...props}>{children}</a>;
}
